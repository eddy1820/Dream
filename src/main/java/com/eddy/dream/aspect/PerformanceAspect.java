package com.eddy.dream.aspect;

import com.eddy.dream.aspect.annotation.LogExecutionTime;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Performance Monitoring Aspect
 * 
 * Automatically logs execution time for:
 * - All Controller, Service, and Repository methods
 * - Methods/classes annotated with @LogExecutionTime
 */
@Slf4j
@Aspect
@Component
public class PerformanceAspect {
    
    // Performance thresholds (milliseconds)
    private static final long SLOW_THRESHOLD = 1000;
    private static final long WARN_THRESHOLD = 500;
    
    // ========== Pointcut Definitions ==========
    
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {}
    
    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceMethods() {}
    
    @Pointcut("within(@org.springframework.stereotype.Repository *)")
    public void repositoryMethods() {}
    
    @Pointcut("@within(com.eddy.dream.aspect.annotation.LogExecutionTime) || " +
              "@annotation(com.eddy.dream.aspect.annotation.LogExecutionTime)")
    public void annotatedMethods() {}
    
    // ========== Around Advice (Simplified) ==========
    
    /**
     * Log execution time for Controller methods
     * Priority: 1 (Lower number = higher priority, but we want annotated to run first)
     */
    @Around("controllerMethods()")
    public Object logControllerExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        // Check if method has @LogExecutionTime annotation
        LogExecutionTime annotation = getAnnotation(joinPoint);
        if (annotation != null) {
            return logExecutionTime(joinPoint, "CONTROLLER", annotation);
        }
        return logExecutionTime(joinPoint, "CONTROLLER", null);
    }
    
    /**
     * Log execution time for Service methods
     */
    @Around("serviceMethods()")
    public Object logServiceExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        // Check if method has @LogExecutionTime annotation
        LogExecutionTime annotation = getAnnotation(joinPoint);
        if (annotation != null) {
            return logExecutionTime(joinPoint, "SERVICE", annotation);
        }
        return logExecutionTime(joinPoint, "SERVICE", null);
    }
    
    /**
     * Log execution time for Repository methods
     */
    @Around("repositoryMethods()")
    public Object logRepositoryExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        // Check if method has @LogExecutionTime annotation
        LogExecutionTime annotation = getAnnotation(joinPoint);
        if (annotation != null) {
            return logExecutionTime(joinPoint, "REPOSITORY", annotation);
        }
        return logExecutionTime(joinPoint, "REPOSITORY", null);
    }
    
    // ========== Core Logging Logic (Unified) ==========
    
    /**
     * Unified method to log execution time with optional custom annotation
     */
    private Object logExecutionTime(ProceedingJoinPoint joinPoint, String layer, LogExecutionTime annotation) 
            throws Throwable {
        
        String className = getClassName(joinPoint);
        String methodName = joinPoint.getSignature().getName();
        String description = (annotation != null && !annotation.value().isEmpty()) 
            ? annotation.value() : methodName;
        
        // Log method entry with optional parameters
        logMethodEntry(layer, className, description, joinPoint.getArgs(), annotation);
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Log successful execution
            logMethodSuccess(layer, className, description, executionTime, result, annotation);
            
            return result;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logMethodFailure(layer, className, description, executionTime, e);
            throw e;
        }
    }
    
    // ========== Helper Methods ==========
    
    private void logMethodEntry(String layer, String className, String description, 
                                 Object[] args, LogExecutionTime annotation) {
        if (annotation != null && annotation.logParams() && args != null && args.length > 0) {
            String params = Arrays.stream(args)
                .map(arg -> arg != null ? arg.toString() : "null")
                .collect(Collectors.joining(", "));
            log.info("[{}] → {}.{}() with params: [{}]", layer, className, description, params);
        } else {
            log.debug("[{}] → {}.{}() started", layer, className, description);
        }
    }
    
    private void logMethodSuccess(String layer, String className, String description, 
                                   long executionTime, Object result, LogExecutionTime annotation) {
        // Check if should log based on threshold
        if (annotation != null && executionTime < annotation.threshold()) {
            return;
        }
        
        String message = buildSuccessMessage(layer, className, description, executionTime, result, annotation);
        
        // Log with appropriate level based on execution time
        if (executionTime >= SLOW_THRESHOLD) {
            log.warn("{} [SLOW]", message);
        } else if (executionTime >= WARN_THRESHOLD) {
            log.info(message);
        } else {
            log.debug(message);
        }
    }
    
    private String buildSuccessMessage(String layer, String className, String description, 
                                        long executionTime, Object result, LogExecutionTime annotation) {
        StringBuilder msg = new StringBuilder();
        msg.append(String.format("[%s] ✓ %s.%s() completed in %d ms", 
            layer, className, description, executionTime));
        
        if (annotation != null && annotation.logResult() && result != null) {
            msg.append(" - Result: ").append(result);
        }
        
        return msg.toString();
    }
    
    private void logMethodFailure(String layer, String className, String description, 
                                   long executionTime, Exception e) {
        log.error("[{}] ✗ {}.{}() failed after {} ms - Error: {}", 
            layer, className, description, executionTime, e.getMessage());
    }
    
    private LogExecutionTime getAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        LogExecutionTime annotation = method.getAnnotation(LogExecutionTime.class);
        if (annotation == null) {
            annotation = joinPoint.getTarget().getClass().getAnnotation(LogExecutionTime.class);
        }
        return annotation;
    }
    
    private String determineLayer(ProceedingJoinPoint joinPoint) {
        Class<?> targetClass = joinPoint.getTarget().getClass();
        
        if (targetClass.isAnnotationPresent(org.springframework.web.bind.annotation.RestController.class)) {
            return "CONTROLLER";
        } else if (targetClass.isAnnotationPresent(org.springframework.stereotype.Service.class)) {
            return "SERVICE";
        } else if (targetClass.isAnnotationPresent(org.springframework.stereotype.Repository.class)) {
            return "REPOSITORY";
        }
        return "CUSTOM";
    }
    
    /**
     * Get the actual class name (handles proxy classes)
     */
    private String getClassName(ProceedingJoinPoint joinPoint) {
        Class<?> targetClass = joinPoint.getTarget().getClass();
        
        // If it's a proxy class (e.g., $Proxy123), try to get the interface name
        if (targetClass.getName().contains("$Proxy")) {
            Class<?>[] interfaces = targetClass.getInterfaces();
            if (interfaces.length > 0) {
                // Return the first interface name (usually the Repository/Service interface)
                return interfaces[0].getSimpleName();
            }
        }
        
        // For CGLIB proxies (e.g., UserServiceImpl$$EnhancerBySpringCGLIB$$123)
        if (targetClass.getName().contains("$$")) {
            return targetClass.getSuperclass().getSimpleName();
        }
        
        // Regular class
        return targetClass.getSimpleName();
    }
}

