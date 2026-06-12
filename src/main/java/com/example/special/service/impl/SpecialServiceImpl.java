package com.example.special.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.example.special.entity.AutoTaskInfo;
import com.example.special.entity.AutoTaskParamInfo;
import com.example.special.entity.AutoTaskStepInfo;
import com.example.special.entity.req.AutoTaskRequest;
import com.example.special.mapper.AutoTaskMapper;
import com.example.special.service.SpecialService;
import com.example.special.utils.DynamicClassLoader;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@AllArgsConstructor
public class SpecialServiceImpl implements SpecialService {
    private final ApplicationContext applicationContext;
    private final AutoTaskMapper autoTaskMapper;
    private JdbcTemplate jdbcTemplate;
    // 每个任务一个独立类加载器，方便卸载
    private final Map<String, DynamicClassLoader> classLoaderMap = new ConcurrentHashMap<>();

    @Override
    public int insertTask(AutoTaskInfo taskInfo) {
        return autoTaskMapper.insertAutoTask(taskInfo);
    }

    @Override
    public int insertSteps(List<AutoTaskStepInfo> steps) {
        return autoTaskMapper.insertAutoTaskStep(steps);
    }

    @Override
    public int insertParams(List<AutoTaskParamInfo> params) {
        return autoTaskMapper.insertAutoTaskParam(params);
    }

    @Override
    public int updateTask(AutoTaskInfo taskInfo) {
        return autoTaskMapper.updateAutoTask(taskInfo);
    }

    @Override
    public int updateStep(AutoTaskStepInfo stepInfo) {
        return autoTaskMapper.updateAutoTaskStep(stepInfo);
    }

    @Override
    public int updateParam(AutoTaskParamInfo paramInfo) {
        return autoTaskMapper.updateAutoTaskParam(paramInfo);
    }

    @Override
    public int deleteTask(String taskId) {
        return autoTaskMapper.deleteAutoTask(taskId);
    }

    @Override
    public int deleteStep(String stepId) {
        return autoTaskMapper.deleteAutoTaskStep(stepId);
    }

    @Override
    public int deleteParam(String paramId) {
        return autoTaskMapper.deleteAutoTaskParam(paramId);
    }

    @Override
    public ResponseEntity<PageInfo<AutoTaskInfo>> getTasksByPage(AutoTaskRequest request) {
        PageHelper.startPage(request.getCurrentPage(), request.getPageSize());
        List<AutoTaskInfo> tasks = autoTaskMapper.getAutoTask(request);
        return ResponseEntity.ok(PageInfo.of(tasks));
    }

    @Override
    public ResponseEntity<PageInfo<AutoTaskStepInfo>> getStepsByPage(AutoTaskRequest request) {
        PageHelper.startPage(request.getCurrentPage(), request.getPageSize());
        List<AutoTaskStepInfo> steps = autoTaskMapper.getAutoTaskStep(request);
        return ResponseEntity.ok(PageInfo.of(steps));
    }

    @Override
    public ResponseEntity<PageInfo<AutoTaskParamInfo>> getParamsByPage(AutoTaskRequest request) {
        PageHelper.startPage(request.getCurrentPage(), request.getPageSize());
        List<AutoTaskParamInfo> params = autoTaskMapper.getAutoTaskParam(request);
        return ResponseEntity.ok(PageInfo.of(params));
    }

    // ======================
    // 【主入口】执行整个任务
    // ======================
    @Override
    public ResponseEntity<Object> executeTask(String taskId) throws Exception {
        //读取数据库配置
        AutoTaskRequest request = new AutoTaskRequest().setTaskId(taskId);
        List<AutoTaskInfo> task = autoTaskMapper.getAutoTask(request);//应该只能获取一个任务
        if (task == null || task.size() != 1) {
            throw new Exception("任务数量不正确");
        }
        AutoTaskInfo taskInfo = task.get(0);
        log.info("开始执行任务,taskId:{}, taskName:{}", taskInfo.getId(), taskInfo.getTaskName());
        List<AutoTaskStepInfo> steps = autoTaskMapper.getAutoTaskStep(request);
        if (steps == null || steps.isEmpty()) {
            throw new Exception("任务 " + taskId + " 没有配置执行步骤");
        }
        List<AutoTaskParamInfo> paramsAllSteps = autoTaskMapper.getAutoTaskParamAllSteps(request);//一次查询就获取全部step的参数
        //给参数按照step分组,key为stepId,value为参数列表
        Map<String, List<AutoTaskParamInfo>> paramsDividedByStep = new HashMap<>();
        for (AutoTaskParamInfo param : paramsAllSteps) {
            if (!paramsDividedByStep.containsKey(param.getStepId())) {
                paramsDividedByStep.put(param.getStepId(), new ArrayList<>());
            }
            paramsDividedByStep.get(param.getStepId()).add(param);
        }
        //为step的返回值准备存储空间
        Map<String, Object> stepResultMap = new HashMap<>();

        for (AutoTaskStepInfo step : steps) {
            Object result = executeStep(step, paramsDividedByStep.get(step.getId()), taskId, stepResultMap);
            stepResultMap.put(step.getId(), result);
        }

        return ResponseEntity.ok(stepResultMap);
    }

    // ======================
    // 执行单步（核心逻辑）
    // ======================
    private Object executeStep(AutoTaskStepInfo step, List<AutoTaskParamInfo> params, String taskId, Map<String, Object> stepResultMap) throws Exception {
        Class<?> clazz;
        Object instance;
        if ("1".equals(step.getClassType())) {
            // 普通类：反射实例化
            clazz = Class.forName(step.getClassFullName());
            instance = clazz.getDeclaredConstructor().newInstance();
        } else if ("2".equals(step.getClassType())) {
            // SpringBean：从上下文拿
            clazz = Class.forName(step.getClassFullName());
            instance = applicationContext.getBean(clazz);
        } else if ("3".equals(step.getClassType())) {
            // 动态类
            String className = step.getClassFullName();
            String sourceCode = step.getDynamicSource();

            // 1. 获取/创建自定义类加载器
            DynamicClassLoader classLoader = classLoaderMap.computeIfAbsent(taskId,
                    k -> new DynamicClassLoader());
            // 2. 检查是否已经加载过该类
            try {
                clazz = classLoader.loadClass(className);
                log.info("动态类已加载，直接复用: {}", className);
            } catch (ClassNotFoundException e) {
                // 未加载过，需要编译并加载
                log.info("动态类未加载，开始编译: {}", className);

                // 3. 源码 → byte[]
                byte[] classBytes = DynamicClassLoader.compileToBytes(className, sourceCode);

                // 4. 类加载器塑造 Class 对象
                clazz = classLoader.defineClassFromBytes(className, classBytes);
            }
            // 5. 当成普通类反射实例化
            instance = clazz.getDeclaredConstructor().newInstance();
        } else if ("4".equals(step.getClassType())) {
            clazz = Class.forName(step.getClassFullName());
            //静态函数不需要生成实例
            instance = null;
        } else {
            throw new RuntimeException("不支持的类类型");
        }

        // ==============================================
        // 构建参数（fixed / json / sql / array）
        // ==============================================
        Object[] args = buildParams(params, stepResultMap);

        // ==============================================
        // 反射调用方法
        // ==============================================
        Method method = findMethod(clazz, step.getMethodName(), params);
        method.setAccessible(true);

        return method.invoke(instance, args);
    }

    // ======================
    // 参数构建（增强版：支持类型转换）
    // ======================
    private Object[] buildParams(List<AutoTaskParamInfo> params, Map<String, Object> stepResultMap) throws ClassNotFoundException {
        if (params == null || params.isEmpty()) {
            return new Object[0];
        }
        Object[] args = new Object[params.size()];
        for (int i = 0; i < params.size(); i++) {
            AutoTaskParamInfo p = params.get(i);

            // 使用 paramClassName 获取目标类型
            String typeClassName = p.getParamClassName();
            Class<?> targetClass;

            // 判断是否为数组类型
            if ("array".equals(p.getParamType())) {
                targetClass = getClassForArray(typeClassName);
            } else {
                targetClass = resolveType(typeClassName);
            }

            switch (p.getParamType()) {
                case "fixed":
                    args[i] = convertFixedValue(p.getParamValue(), targetClass);
                    break;
                case "array":
                    args[i] = convertToArray(p.getParamValue(), targetClass);
                    break;
                case "json":
                    if (targetClass == null) {
                        throw new RuntimeException("JSON类型参数必须指定paramClassName");
                    }
                    try {
                        args[i] = JSONUtil.toBean(p.getParamValue(), targetClass);
                    } catch (Exception e) {
                        throw new RuntimeException("JSON转换失败: " + e.getMessage(), e);
                    }
                    break;
                case "sql":
                    args[i] = executeSqlAndConvert(p.getParamValue(), targetClass);
                    break;
                case "result":
                    if (p.getFromStepId() == null || p.getFromStepId().isEmpty()) {
                        throw new RuntimeException("result类型参数必须指定fromStepId");
                    }
                    Object result = stepResultMap.get(p.getFromStepId());
                    args[i] = convertResultValue(result, targetClass);
                    break;
                default:
                    throw new RuntimeException("不支持的参数类型: " + p.getParamType());
            }
        }
        return args;
    }

    // ======================
    // 固定值类型转换
    // ======================
    private Object convertFixedValue(String value, Class<?> targetClass) {
        if (value == null) {
            return null;
        }

        // 如果未指定目标类型，直接返回字符串
        if (targetClass == null || targetClass == String.class) {
            return value;
        }

        // 基本类型及其包装类转换
        if (targetClass == Integer.class || targetClass == int.class) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new RuntimeException("无法将 '" + value + "' 转换为 Integer", e);
            }
        }

        if (targetClass == Long.class || targetClass == long.class) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                throw new RuntimeException("无法将 '" + value + "' 转换为 Long", e);
            }
        }

        if (targetClass == Double.class || targetClass == double.class) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                throw new RuntimeException("无法将 '" + value + "' 转换为 Double", e);
            }
        }

        if (targetClass == Float.class || targetClass == float.class) {
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException e) {
                throw new RuntimeException("无法将 '" + value + "' 转换为 Float", e);
            }
        }

        if (targetClass == Boolean.class || targetClass == boolean.class) {
            return Boolean.parseBoolean(value);
        }

        if (targetClass == Short.class || targetClass == short.class) {
            try {
                return Short.parseShort(value);
            } catch (NumberFormatException e) {
                throw new RuntimeException("无法将 '" + value + "' 转换为 Short", e);
            }
        }

        if (targetClass == Byte.class || targetClass == byte.class) {
            try {
                return Byte.parseByte(value);
            } catch (NumberFormatException e) {
                throw new RuntimeException("无法将 '" + value + "' 转换为 Byte", e);
            }
        }

        if (targetClass == Character.class || targetClass == char.class) {
            if (value.length() != 1) {
                throw new RuntimeException("Char类型参数必须是单个字符，当前值: '" + value + "'");
            }
            return value.charAt(0);
        }
        return JSONUtil.toBean(value, targetClass);
    }

    // ======================
    // SQL查询并转换结果
    // ======================
    private Object executeSqlAndConvert(String sql, Class<?> targetClass) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new RuntimeException("SQL不能为空");
        }

        try {
            // 判断是查询单条还是多条
            if (targetClass != null && !targetClass.equals(Map.class)) {
                // 转换为指定实体类
                return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                    try {
                        Object instance = targetClass.getDeclaredConstructor().newInstance();
                        java.sql.ResultSetMetaData metaData = rs.getMetaData();
                        int columnCount = metaData.getColumnCount();

                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnLabel(i);
                            Object value = rs.getObject(i);

                            // 尝试通过setter方法设置值
                            String setterName = "set" + capitalizeFirstLetter(columnName);
                            try {
                                Method setter = targetClass.getMethod(setterName, value.getClass());
                                setter.invoke(instance, value);
                            } catch (NoSuchMethodException e) {
                                // 尝试直接设置字段
                                try {
                                    java.lang.reflect.Field field = targetClass.getDeclaredField(columnName);
                                    field.setAccessible(true);
                                    field.set(instance, value);
                                } catch (Exception ex) {
                                    // 忽略无法设置的字段
                                }
                            }
                        }
                        return instance;
                    } catch (Exception e) {
                        throw new RuntimeException("映射SQL结果到实体类失败: " + e.getMessage(), e);
                    }
                });
            } else {
                // 默认返回Map
                return jdbcTemplate.queryForMap(sql);
            }
        } catch (Exception e) {
            throw new RuntimeException("执行SQL失败: " + sql + ", 错误: " + e.getMessage(), e);
        }
    }

    // ======================
    // 结果值类型转换
    // ======================
    private Object convertResultValue(Object result, Class<?> targetClass) {
        if (result == null) {
            return null;
        }

        // 如果未指定目标类型或类型匹配，直接返回
        if (targetClass == null || targetClass.isInstance(result)) {
            return result;
        }

        // 尝试类型转换
        if (targetClass.isAssignableFrom(result.getClass())) {
            return result;
        }

        // 数字类型转换
        if (targetClass == Integer.class || targetClass == int.class) {
            if (result instanceof Number) {
                return ((Number) result).intValue();
            }
            try {
                return Integer.parseInt(result.toString());
            } catch (NumberFormatException e) {
                throw new RuntimeException("无法将结果 '" + result + "' 转换为 Integer", e);
            }
        }

        if (targetClass == Long.class || targetClass == long.class) {
            if (result instanceof Number) {
                return ((Number) result).longValue();
            }
            try {
                return Long.parseLong(result.toString());
            } catch (NumberFormatException e) {
                throw new RuntimeException("无法将结果 '" + result + "' 转换为 Long", e);
            }
        }

        if (targetClass == Double.class || targetClass == double.class) {
            if (result instanceof Number) {
                return ((Number) result).doubleValue();
            }
            try {
                return Double.parseDouble(result.toString());
            } catch (NumberFormatException e) {
                throw new RuntimeException("无法将结果 '" + result + "' 转换为 Double", e);
            }
        }

        if (targetClass == String.class) {
            return result.toString();
        }

        // JSON转换（如果结果是JSON字符串）
        if (result instanceof String) {
            try {
                return JSONUtil.toBean((String) result, targetClass);
            } catch (Exception e) {
                // 忽略JSON转换错误
            }
        }

        throw new RuntimeException("无法将类型 " + result.getClass().getName() +
                " 转换为目标类型 " + targetClass.getName());
    }

    // ======================
    // 辅助方法：首字母大写
    // ======================
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    // ======================
    // 类型解析（支持基本类型名称和全类名）
    // ======================
    private static Class<?> resolveType(String typeName) throws ClassNotFoundException {
        if (StrUtil.isBlank(typeName)) {
            throw new ClassNotFoundException("类名不能为空");
        }

        // 处理基本类型
        switch (typeName.toLowerCase()) {
            case "int":
            case "integer":
                return int.class;
            case "long":
                return long.class;
            case "double":
                return double.class;
            case "float":
                return float.class;
            case "boolean":
                return boolean.class;
            case "short":
                return short.class;
            case "byte":
                return byte.class;
            case "char":
            case "character":
                return char.class;
            case "void":
                return void.class;
        }

        // 处理基本类型的包装类（也支持全类名）
        switch (typeName) {
            case "java.lang.Integer":
            case "Integer":
                return Integer.class;
            case "java.lang.Long":
            case "Long":
                return Long.class;
            case "java.lang.Double":
            case "Double":
                return Double.class;
            case "java.lang.Float":
            case "Float":
                return Float.class;
            case "java.lang.Boolean":
            case "Boolean":
                return Boolean.class;
            case "java.lang.Short":
            case "Short":
                return Short.class;
            case "java.lang.Byte":
            case "Byte":
                return Byte.class;
            case "java.lang.Character":
            case "Character":
                return Character.class;
            case "java.lang.String":
            case "String":
                return String.class;
            case "java.lang.Object":
            case "Object":
                return Object.class;
        }

        // 其他类型，使用全类名加载
        return Class.forName(typeName);
    }

    // 方法匹配
    private Method findMethod(Class<?> clazz, String methodName, List<AutoTaskParamInfo> params) {
        try {
            if (params == null) {
                return clazz.getDeclaredMethod(methodName);
            }
            Class<?>[] paramTypes = new Class<?>[params.size()];
            for (int i = 0; i < params.size(); i++) {
                String clsName = params.get(i).getParamClassName();
                //数组的话
                if ("array".equals(params.get(i).getParamType())) {
                    paramTypes[i] = getClassForArray(clsName);
                } else {
                    paramTypes[i] = resolveType(clsName);
                }
            }
            return clazz.getDeclaredMethod(methodName, paramTypes);
        } catch (Exception e) {
            throw new RuntimeException("找不到方法：" + methodName + "，请检查参数类型", e);
        }
    }

    private static Class<?> getClassForArray(String elementTypeName) throws ClassNotFoundException {
        if (StrUtil.isBlank(elementTypeName)) {
            throw new ClassNotFoundException("数组元素类型不能为空");
        }

        // 先解析元素类型
        Class<?> elementType = resolveType(elementTypeName);

        // 创建数组类型
        if (elementType.isPrimitive()) {
            // 基本类型数组
            switch (elementType.getName()) {
                case "int":
                    return int[].class;
                case "long":
                    return long[].class;
                case "double":
                    return double[].class;
                case "float":
                    return float[].class;
                case "boolean":
                    return boolean[].class;
                case "short":
                    return short[].class;
                case "byte":
                    return byte[].class;
                case "char":
                    return char[].class;
                default:
                    throw new ClassNotFoundException("不支持的基本类型数组: " + elementType.getName());
            }
        } else {
            // 对象类型数组：使用 Array.newInstance 获取数组类
            return java.lang.reflect.Array.newInstance(elementType, 0).getClass();
        }
    }
    private Object convertToArray(String value, Class<?> arrayClass) {
        if (value == null || value.trim().isEmpty()) {
            return java.lang.reflect.Array.newInstance(arrayClass.getComponentType(), 0);
        }

        // 去除首尾的中括号（如果存在）
        String trimmedValue = value.trim();
        if (trimmedValue.startsWith("[") && trimmedValue.endsWith("]")) {
            trimmedValue = trimmedValue.substring(1, trimmedValue.length() - 1);
        }

        // 按逗号分割
        String[] elements = trimmedValue.split(",");
        Class<?> componentType = arrayClass.getComponentType();
        Object array = java.lang.reflect.Array.newInstance(componentType, elements.length);

        for (int i = 0; i < elements.length; i++) {
            String element = elements[i].trim();
            Object convertedElement = convertFixedValue(element, componentType);
            java.lang.reflect.Array.set(array, i, convertedElement);
        }

        return array;
    }
}
