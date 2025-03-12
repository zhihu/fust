package com.zhihu.fust.core.logging.simple;

import com.zhihu.fust.core.logging.LogLevel;
import com.zhihu.fust.core.logging.LoggerConfiguration;
import com.zhihu.fust.core.logging.spi.LoggingSystemInfo;
import com.zhihu.fust.core.logging.spi.ILoggingSystem;

public class SimpleLoggingSystem implements ILoggingSystem {

    public static final SimpleLoggingSystem INSTANCE = new SimpleLoggingSystem();

    @Override
    public void setLogLevel(String loggerName, LogLevel level) {
        throw new UnsupportedOperationException("Unable to set log level");
    }

    @Override
    public LoggerConfiguration getLoggerConfiguration(String loggerName) {
        return LoggerConfiguration.DEFAULT;
    }

    @Override
    public LoggingSystemInfo getLoggingSystemInfo() {
        return new SimpleLoggingSystemInfo();
    }
}
