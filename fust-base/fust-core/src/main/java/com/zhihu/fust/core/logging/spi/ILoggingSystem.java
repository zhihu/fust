package com.zhihu.fust.core.logging.spi;

import com.zhihu.fust.commons.lang.SpiServiceLoader;
import com.zhihu.fust.core.logging.LogLevel;
import com.zhihu.fust.core.logging.LoggerConfiguration;
import com.zhihu.fust.core.logging.simple.SimpleLoggingSystem;

/**
 * Common abstraction over logging systems.
 */
public interface ILoggingSystem {

    /**
     * The name used for the root logger. ILoggingSystem implementations should ensure that
     * this is the name used to represent the root logger, regardless of the underlying
     * implementation.
     */
    String ROOT_LOGGER_NAME = "ROOT";

    /**
     * 获取当前的日志系统实现
     *
     * @return logging system
     */
    static ILoggingSystem get() {
        return SpiServiceLoader.get(ILoggingSystem.class)
                               .orElse(SimpleLoggingSystem.INSTANCE);
    }

    /**
     * 使用日志配置文件初始化日志系统
     */
    default void initialize() {
    }

    /**
     * 立即执行配置文件变更检查，让配置快速生效
     */
    default void checkFiles() {
    }

    /**
     * Sets the logging level for a given logger.
     *
     * @param loggerName the name of the logger to set ({@code null} can be used for the
     *                   root logger).
     * @param level      the log level ({@code null} can be used to remove any custom level for
     *                   the logger and use the default configuration instead)
     */
    void setLogLevel(String loggerName, LogLevel level);

    /**
     * Returns the current configuration for a {@link ILoggingSystem}'s logger.
     *
     * @param loggerName the name of the logger
     * @return the current configuration
     */
    LoggerConfiguration getLoggerConfiguration(String loggerName);

    LoggingSystemInfo getLoggingSystemInfo();
}
