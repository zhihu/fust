package com.zhihu.fust.core.logging;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * log constants
 */
public final class LogConstants {
    public static final String LOG_CFG_DIR_KEY = "log.cfg.dir";
    public static final String FILE_LOG_DIR_KEY = "file.log.dir";
    public static final String PATTERN_KEY = "logging.pattern";
    public static final String FILE_BASE_SIZE_KEY = "file.base.size";
    public static final String FILE_MAX_SIZE_KEY = "file.max.size";
    public static final String MAX_HISTORY_KEY = "file.max.history";
    public static final String CONSOLE_LEVEL_KEY = "console.level";
    public static final String FILE_LEVEL_KEY = "file.level";
    public static final String ROOT_LEVEL_KEY = "root.level";
    public static final String ROOT_LEVEL_PROPERTY_NAME = "logging.level.root";
    public static final String ROOT_LEVEL_ENV_NAME = "LOGGING_LEVEL_ROOT";
    public static final String LOGGER_PLACEHOLDER = "<!-- logger-placeholder -->";
    public static final String FILTER_PLACEHOLDER = "<!-- filter-placeholder -->";
    public static final String LOGGER_LEVEL_FMT = "\t<logger name=\"%s\" level=\"%s\"/>\n";
    public static final String DEFAULT_MAX_HISTORY = "6";
    public static final String DEFAULT_TOTAL_SIZE_CAP = "300 MB";
    public static final String DEFAULT_FILE_BASE_SIZE = "50 MB";

    private static final Set<String> VALID_LEVELS = new HashSet<>(
            Arrays.asList("DEBUG", "INFO", "WARN", "ERROR"));
    public static final String CONFIG_PROPERTY = "logging.config";

    public static String getDefaultValue(String key) {
        if (key.equalsIgnoreCase(FILE_MAX_SIZE_KEY)) {
            return DEFAULT_TOTAL_SIZE_CAP;
        }

        if (key.equalsIgnoreCase(MAX_HISTORY_KEY)) {
            return DEFAULT_MAX_HISTORY;
        }

        if (key.equalsIgnoreCase(FILE_BASE_SIZE_KEY)) {
            return DEFAULT_FILE_BASE_SIZE;
        }
        return "";
    }

    public static String normalize(String level) {
        level = Optional.ofNullable(level).orElse("").toUpperCase();
        if (!LogConstants.VALID_LEVELS.contains(level)) {
            level = LogLevel.ERROR.name();
        }
        return level;
    }

    public static String getWorkDir() {
        return System.getProperty("user.dir");
    }

    /**
     * 目前仅支持："DEBUG", "INFO", "WARN", "ERROR"
     * 不区分大小写
     */
    public static boolean isValidLevel(String level) {
        level = Optional.ofNullable(level).orElse("").toUpperCase();
        return VALID_LEVELS.contains(level);
    }

    /**
     * 在分支联调以及调试容器的场景下，始终使用 INFO 或更低的日志级别
     */
    public static boolean isForceInfo() {
        return false;
    }

    private LogConstants() {
    }
}
