package com.tmforum.openapi.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Arrays;

/**
 * Aspect for automatic logging of service and controller methods
 */
@Aspect
@Component
public class LoggingAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    
    @Pointcut("execution(* com.tmforum.openapi.service.*.*(..))")
    public void serviceMethods() {}
    
    @Pointcut("execution(* com.tmforum.openapi.controller.*.*(..))")
    public void controllerMethods() {}
    
    @Around("serviceMethods() || controllerMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        // Log entry
        logger.debug("Entering method: {}.{}() with arguments: {}", 
            className, methodName, Arrays.toString(args));
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();
            
            // Log successful exit
            logger.debug("Method {}.{}() executed successfully in {} ms", 
                className, methodName, stopWatch.getTotalTimeMillis());
            
            return result;
        } catch (Exception e) {
            stopWatch.stop();
            
            // Log error
            logger.error("Error in method {}.{}() after {} ms: {}", 
                className, methodName, stopWatch.getTotalTimeMillis(), e.getMessage(), e);
            
            throw e;
        }
    }
}


