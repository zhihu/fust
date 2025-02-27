package com.zhihu.fust.core.logging;

import java.util.Objects;

/**
 * Immutable class that represents the configuration of a log system
 */
public final class LoggerConfiguration {

    public static final LoggerConfiguration DEFAULT = new LoggerConfiguration("DEFAULT", LogLevel.INFO,
                                                                              LogLevel.INFO);

    private final String name;

    private final LogLevel configuredLevel;

    private final LogLevel effectiveLevel;

    /**
     * Create a new {@link LoggerConfiguration instance}.
     *
     * @param name            the name of the logger
     * @param configuredLevel the configured level of the logger
     * @param effectiveLevel  the effective level of the logger
     */
    public LoggerConfiguration(String name, LogLevel configuredLevel, LogLevel effectiveLevel) {
        Objects.requireNonNull(name, "Name must not be null");
        Objects.requireNonNull(effectiveLevel, "EffectiveLevel must not be null");
        this.name = name;
        this.configuredLevel = configuredLevel;
        this.effectiveLevel = effectiveLevel;
    }

    /**
     * Returns the configured level of the logger.
     *
     * @return the configured level of the logger
     */
    public LogLevel getConfiguredLevel() {
        return this.configuredLevel;
    }

    /**
     * Returns the effective level of the logger.
     *
     * @return the effective level of the logger
     */
    public LogLevel getEffectiveLevel() {
        return this.effectiveLevel;
    }

    /**
     * Returns the name of the logger.
     *
     * @return the name of the logger
     */
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "LoggerConfiguration [name=" + this.name + ", configuredLevel=" + this.configuredLevel
               + ", effectiveLevel=" + this.effectiveLevel + "]";
    }

}
