package com.zhihu.fust.core.logging.spi;

import com.zhihu.fust.core.logging.LogLevel;

/**
 * manage log configuration
 */
public interface LoggingConfigManager {
    /**
     * root log level
     *
     * @return root log level
     */
    LogLevel getInitRootLevel();

    /**
     * 获取当前的根日志级别
     *
     * @return root log level
     */
    LogLevel getRootLevel();

    void initLogConfig();

    String getLogCfgFilePath();

}
