package com.zhihu.fust.spring.toolkit.log;

import com.zhihu.fust.telemetry.api.TraceUtils;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.time.Duration;

/**
 * AOP for spring bean method invoke
 * Usage:
 * 1. create MethodLogAspect to context
 * 2. add @EnableAspectJAutoProxy to enable AOP
 * 3. use @MethodLog to mark the method
 * <p>
 */
@Aspect
public class MethodLogAspect {

    private final MethodLogger methodLogger;


    public MethodLogAspect() {
        this(new DefaultMethodLogger());
    }

    public MethodLogAspect(MethodLogger methodLogger) {
        this.methodLogger = methodLogger;
    }

    @Pointcut("@annotation(methodLog)")
    public void scheduledLogPointcut(MethodLog methodLog) {
    }

    @Around("scheduledLogPointcut(methodLog)")
    public void logScheduledExecution(ProceedingJoinPoint joinPoint, MethodLog methodLog) throws Throwable {
        // 注入 traceId
        Context context = TraceUtils.ensureTraceId();
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        try (Scope scope = context.makeCurrent()) {
            methodLogger.onStart(methodName, methodLog);
            joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            methodLogger.onEnd(methodName, methodLog, Duration.ofMillis(endTime - startTime));
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            methodLogger.onError(methodName, methodLog, Duration.ofMillis(endTime - startTime), e);
            throw e;
        }
    }
}