package com.zhihu.fust.spring.toolkit.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Default logger for invoke logger
 */
public class DefaultMethodLogger implements MethodLogger {

    private static final Logger log = LoggerFactory.getLogger(DefaultMethodLogger.class);

    @Override
    public void onStart(String methodName, MethodLog methodLog) {
        log.info("start: {}, desc: {}", methodName, methodLog.desc());
    }

    @Override
    public void onEnd(String methodName, MethodLog methodLog, Duration duration) {
        log.info("scheduled task end: {}, costTime: {} ms", methodName, duration.toMillis());
    }

    @Override
    public void onError(String methodName, MethodLog methodLog, Duration duration, Throwable throwable) {
        log.error("scheduled task error: {}, desc: {} costTime:{}", methodName, methodLog.desc(), duration.toMillis(), throwable);
    }
}
