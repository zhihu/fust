package com.zhihu.fust.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogTest {
    private static final Logger logger = LoggerFactory.getLogger(LogTest.class);

    public static void run() {
        logger.info("info message");
        logger.warn("warn message");
        logger.error("error message");
    }
}
