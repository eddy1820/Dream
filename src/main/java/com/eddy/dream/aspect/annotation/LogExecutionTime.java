package com.eddy.dream.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to log method execution time
 * 
 * Can be applied to methods or classes (will apply to all methods in the class)
 * 
 * Usage:
 * - @LogExecutionTime on a method
 * - @LogExecutionTime on a class (applies to all public methods)
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecutionTime {
    
    /**
     * Custom description for the operation being logged
     * If empty, will use the method name
     */
    String value() default "";
    
    /**
     * Whether to log method parameters
     */
    boolean logParams() default true;
    
    /**
     * Whether to log return value
     */
    boolean logResult() default false;
    
    /**
     * Log level threshold in milliseconds
     * Only log if execution time exceeds this value (0 = always log)
     */
    long threshold() default 0;
}

