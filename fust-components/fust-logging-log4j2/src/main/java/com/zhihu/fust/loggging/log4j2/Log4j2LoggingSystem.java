package com.zhihu.fust.loggging.log4j2;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.core.util.NameUtil;
import org.apache.logging.log4j.core.util.WatchManager;
import org.apache.logging.log4j.message.Message;

import com.zhihu.fust.commons.lang.SpiServiceLoader;
import com.zhihu.fust.commons.lang.StringUtils;
import com.zhihu.fust.core.logging.LogLevel;
import com.zhihu.fust.core.logging.LogLevelConverter;
import com.zhihu.fust.core.logging.LoggerConfiguration;
import com.zhihu.fust.core.logging.spi.LoggingSystemInfo;
import com.zhihu.fust.core.logging.spi.ILoggingSystem;
import com.zhihu.fust.core.logging.spi.LoggingConfigManager;

/**
 * 日志系统
 */
public class Log4j2LoggingSystem implements ILoggingSystem {
    private static final LogLevelConverter<Level> LEVELS = new LogLevelConverter<>();
    private final LoggingConfigManager loggingConfigManager;

    static {
        LEVELS.map(LogLevel.TRACE, Level.TRACE);
        LEVELS.map(LogLevel.DEBUG, Level.DEBUG);
        LEVELS.map(LogLevel.INFO, Level.INFO);
        LEVELS.map(LogLevel.WARN, Level.WARN);
        LEVELS.map(LogLevel.ERROR, Level.ERROR);
        LEVELS.map(LogLevel.FATAL, Level.FATAL);
        LEVELS.map(LogLevel.OFF, Level.OFF);
    }

    LoggingConfigManager getLoggingConfigManager() {
        return loggingConfigManager;
    }

    private static final Filter FILTER = new AbstractFilter() {

        @Override
        public Result filter(LogEvent event) {
            return Result.DENY;
        }

        @Override
        public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
            return Result.DENY;
        }

        @Override
        public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
            return Result.DENY;
        }

        @Override
        public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
            return Result.DENY;
        }

    };

    public Log4j2LoggingSystem() {
        loggingConfigManager = SpiServiceLoader.get(LoggingConfigManager.class)
                .orElse(new Log4j2LoggingConfigManager());

        configureRootLevel();
    }

    private void configureRootLevel() {
        LogLevel rootLevel = loggingConfigManager.getInitRootLevel();
        Level level = Level.toLevel(rootLevel.name(), Level.WARN);
        Configurator.setRootLevel(level);
    }

    @Override
    public void initialize() {
        loggingConfigManager.initLogConfig();
        // config to system
        String configLocation = loggingConfigManager.getLogCfgFilePath();
        LoggerContext loggerContext = getLoggerContext();
        if (isAlreadyInitialized(loggerContext)) {
            return;
        }
        // 移除日志
        loggerContext.getConfiguration().removeFilter(FILTER);
        loadConfiguration(configLocation);
        markAsInitialized(loggerContext);

        // register shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                getLoggerContext().stop();
            } catch (ClassCastException e) {
                // ignore
            }
        }));
    }

    @Override
    public void checkFiles() {
        WatchManager watchManager = getLoggerContext().getConfiguration().getWatchManager();
        watchManager.checkFiles();
    }

    protected void loadConfiguration(String location) {
        Objects.requireNonNull(location, "Location must not be null");
        try {
            LoggerContext ctx = getLoggerContext();
            File file = new File(location);
            URL url = file.toURI().toURL();
            InputStream stream = url.openStream();
            ConfigurationSource source = new ConfigurationSource(stream, file);
            ctx.start(ConfigurationFactory.getInstance().getConfiguration(ctx, source));
        } catch (Exception ex) {
            throw new IllegalStateException("Could not initialize Log4J2 logging from " + location, ex);
        }
    }

    @Override
    public LoggerConfiguration getLoggerConfiguration(String loggerName) {
        LoggerConfig loggerConfig = getAllLoggers().get(loggerName);
        return (loggerConfig != null) ? convertLoggerConfig(loggerName, loggerConfig) : null;
    }

    @Override
    public LoggingSystemInfo getLoggingSystemInfo() {
        return SpiServiceLoader.get(LoggingSystemInfo.class).orElse(new Log4j2LoggingSystemInfo());
    }

    private LoggerConfiguration convertLoggerConfig(String name, LoggerConfig loggerConfig) {
        if (loggerConfig == null) {
            return null;
        }
        LogLevel level = LEVELS.convertNativeToSystem(loggerConfig.getLevel());
        if (StringUtils.isBlank(name) || LogManager.ROOT_LOGGER_NAME.equals(name)) {
            name = ROOT_LOGGER_NAME;
        }
        boolean isLoggerConfigured = loggerConfig.getName().equals(name);
        LogLevel configuredLevel = (isLoggerConfigured) ? level : null;
        return new LoggerConfiguration(name, configuredLevel, level);
    }

    private Map<String, LoggerConfig> getAllLoggers() {
        Map<String, LoggerConfig> loggers = new LinkedHashMap<>();
        for (Logger logger : getLoggerContext().getLoggers()) {
            addLogger(loggers, logger.getName());
        }
        getLoggerContext().getConfiguration().getLoggers().keySet().forEach(name -> addLogger(loggers, name));
        return loggers;
    }

    private void addLogger(Map<String, LoggerConfig> loggers, String name) {
        Configuration configuration = getLoggerContext().getConfiguration();
        while (name != null) {
            loggers.computeIfAbsent(name, configuration::getLoggerConfig);
            name = getSubName(name);
        }
    }

    private String getSubName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        int nested = name.lastIndexOf('$');
        return (nested != -1) ? name.substring(0, nested) : NameUtil.getSubName(name);
    }

    @Override
    public void setLogLevel(String loggerName, LogLevel logLevel) {
        Level level = LEVELS.convertSystemToNative(logLevel);
        LoggerConfig logger = getLogger(loggerName);
        if (logger == null) {
            logger = new LoggerConfig(loggerName, level, true);
            getLoggerContext().getConfiguration().addLogger(loggerName, logger);
        } else {
            logger.setLevel(level);
        }
        getLoggerContext().updateLoggers();
    }

    private LoggerConfig getLogger(String name) {
        boolean isRootLogger = StringUtils.isBlank(name) || ROOT_LOGGER_NAME.equals(name);
        return findLogger(isRootLogger ? LogManager.ROOT_LOGGER_NAME : name);
    }

    private LoggerConfig findLogger(String name) {
        Configuration configuration = getLoggerContext().getConfiguration();
        if (configuration instanceof AbstractConfiguration) {
            return ((AbstractConfiguration) configuration).getLogger(name);
        }
        return configuration.getLoggers().get(name);
    }

    private LoggerContext getLoggerContext() {
        return (LoggerContext) LogManager.getContext(false);
    }

    private boolean isAlreadyInitialized(LoggerContext loggerContext) {
        return ILoggingSystem.class.getName().equals(loggerContext.getExternalContext());
    }

    private void markAsInitialized(LoggerContext loggerContext) {
        loggerContext.setExternalContext(ILoggingSystem.class.getName());
    }
}
