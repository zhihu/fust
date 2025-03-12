package com.zhihu.fust.core.logging.simple;

import com.zhihu.fust.core.logging.LogLevel;
import com.zhihu.fust.core.logging.spi.LoggingConfigManager;

public class SimpleLogConfigManager implements LoggingConfigManager {
    public static final SimpleLogConfigManager INSTANCE = new SimpleLogConfigManager();

    @Override
    public LogLevel getInitRootLevel() {
        return LogLevel.INFO;
    }

    @Override
    public LogLevel getRootLevel() {
        return LogLevel.INFO;
    }

    @Override
    public void initLogConfig() {

    }

    @Override
    public String getLogCfgFilePath() {
        return null;
    }
}
