package com.example.special.service.impl;

import cn.hutool.json.JSONUtil;
import com.example.special.entity.AutoTaskInfo;
import com.example.special.entity.AutoTaskParamInfo;
import com.example.special.entity.AutoTaskStepInfo;
import com.example.special.entity.req.AutoTaskRequest;
import com.example.special.mapper.AutoTaskMapper;
import com.example.special.service.SpecialService;
import com.example.special.utils.DynamicClassLoader;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    // ======================
    // 【主入口】执行整个任务
    // ======================
    @Override
    public Object executeTask(String taskId) throws Exception {
        //读取数据库配置
        AutoTaskRequest request = new AutoTaskRequest().setTaskId(taskId);
        List<AutoTaskInfo> task = autoTaskMapper.getAutoTask(request);//应该只能获取一个任务
        AutoTaskInfo taskInfo = task.get(0);
        List<AutoTaskStepInfo> steps = autoTaskMapper.getAutoTaskStep(request);
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
        List<Object> resultList = new ArrayList<>();

        //按顺序执行每一步
        for (AutoTaskStepInfo step : steps) {
            Object result = executeStep(step, paramsDividedByStep.get(step.getId()), taskId);
            resultList.add(result);
        }

        return resultList;
    }

    // ======================
    // 执行单步（核心逻辑）
    // ======================
    private Object executeStep(AutoTaskStepInfo step, List<AutoTaskParamInfo> params, String taskId) throws Exception {
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

            // 2. 源码 → byte[]
            byte[] classBytes = DynamicClassLoader.compileToBytes(className,sourceCode);

            // 3. 类加载器塑造 Class 对象
            clazz = classLoader.defineClassFromBytes(className, classBytes);

            // 4. 当成普通类反射实例化
            instance = clazz.getDeclaredConstructor().newInstance();
        } else {
            throw new RuntimeException("不支持的类类型");
        }

        // ==============================================
        // 构建参数（fixed / json / sql）
        // ==============================================
        Object[] args = buildParams(params);

        // ==============================================
        // 反射调用方法
        // ==============================================
        Method method = findMethod(clazz, step.getMethodName(), args.length);
        method.setAccessible(true);
        return method.invoke(instance, args);
    }

    // ======================
    // 参数构建（你之前的逻辑）
    // ======================
    private Object[] buildParams(List<AutoTaskParamInfo> params) throws ClassNotFoundException {
        Object[] args = new Object[params.size()];
        for (int i = 0; i < params.size(); i++) {
            AutoTaskParamInfo p = params.get(i);
            switch (p.getParamType()) {
                case "fixed":
                    args[i] = p.getParamValue();
                    break;
                case "json":
                    args[i] = JSONUtil.toBean(p.getParamValue(), Class.forName(p.getParamClassName()));
                    break;
                case "sql":
                    args[i] = jdbcTemplate.queryForMap(p.getParamValue());
                    break;
            }
        }
        return args;
    }

    // 方法匹配
    private Method findMethod(Class<?> clazz, String methodName, int argCount) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(methodName) && m.getParameterCount() == argCount) {
                return m;
            }
        }
        throw new RuntimeException("方法不存在：" + methodName);
    }
}
