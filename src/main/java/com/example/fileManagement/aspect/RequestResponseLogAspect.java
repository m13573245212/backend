package com.example.fileManagement.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RequestResponseLogAspect {

    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object logRequestResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        // 记录请求开始时间
        long startTime = System.currentTimeMillis();
        LocalDateTime now = LocalDateTime.now();

        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        String[] paramNames = signature.getParameterNames();
        Object[] paramValues = joinPoint.getArgs();

        // 打印请求日志
        log.info("┌──────────────────────────────────────────────────────────────");
        log.info("│ 请求时间: {}", now.format(FORMATTER));
        if (request != null) {
            log.info("│ 请求路径: {} {}", request.getMethod(), request.getRequestURI());
            log.info("│ 请求IP: {}", request.getRemoteAddr());
        }
        log.info("│ 控制器: {}.{}", className, methodName);
        
        // 打印请求参数
        if (paramNames != null && paramNames.length > 0) {
            log.info("│ 请求参数:");
            for (int i = 0; i < paramNames.length; i++) {
                Object value = paramValues[i];
                String valueStr;
                try {
                    // 如果是基本类型或字符串，直接显示；否则转为JSON
                    if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean) {
                        valueStr = String.valueOf(value);
                    } else {
                        valueStr = objectMapper.writeValueAsString(value);
                    }
                } catch (Exception e) {
                    valueStr = value != null ? value.getClass().getName() : "null";
                }
                log.info("│   {} = {}", paramNames[i], valueStr);
            }
        }

        Object result = null;
        Exception exception = null;

        try {
            // 执行目标方法
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            // 计算耗时
            long duration = System.currentTimeMillis() - startTime;

            // 打印响应日志
            log.info("│ 响应耗时: {}ms", duration);
            
            if (exception != null) {
                log.info("│ 响应状态: 异常");
                log.info("│ 异常信息: {}", exception.getMessage());
            } else {
                log.info("│ 响应状态: 成功");
                // 打印响应结果（过长时截断）
                if (result != null) {
                    try {
                        String responseJson = objectMapper.writeValueAsString(result);
                        if (responseJson.length() > 1000) {
                            responseJson = responseJson.substring(0, 1000) + "...(truncated)";
                        }
                        log.info("│ 响应内容: {}", responseJson);
                    } catch (Exception e) {
                        log.info("│ 响应内容: {}", result.getClass().getName());
                    }
                }
            }
            log.info("└──────────────────────────────────────────────────────────────");
        }
    }
}