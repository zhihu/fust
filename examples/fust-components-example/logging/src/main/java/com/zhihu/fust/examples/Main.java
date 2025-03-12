package com.zhihu.fust.examples;

import com.zhihu.fust.core.env.Env;
import com.zhihu.fust.core.logging.spi.ILoggingSystem;
import com.zhihu.fust.telemetry.api.TraceUtils;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void run() {
        logger.info("info message");
        logger.warn("warn message");
        logger.error("error message");
    }

    public static void main(String[] args) {
        Env.init();
        ILoggingSystem loggingSystem = ILoggingSystem.get();
        loggingSystem.initialize();

        Context context = TraceUtils.ensureTraceId();
        try (Scope scope = context.makeCurrent()) {
            logger.info("traceId: {}", TraceUtils.getTraceId());
            run();
            LogTest.run();
        }
    }
}
