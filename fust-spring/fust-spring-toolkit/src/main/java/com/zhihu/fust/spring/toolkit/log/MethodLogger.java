package com.zhihu.fust.spring.toolkit.log;

import java.time.Duration;

/**
 * a logger for invoke method
 */
public interface MethodLogger {

    /**
     * when schedule task start
     */
    void onStart(String methodName, MethodLog methodLog);

    /**
     * when schedule task end
     */
    void onEnd(String methodName, MethodLog methodLog, Duration duration);

    /**
     * when schedule task error
     */
    void onError(String methodName, MethodLog methodLog, Duration duration, Throwable throwable);
}
