package com.zhihu.fust.core.logging;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * LogLevel converter
 */
public final class LogLevelConverter<T> {

    private final Map<LogLevel, T> systemToNative;

    private final Map<T, LogLevel> nativeToSystem;

    public LogLevelConverter() {
        this.systemToNative = new EnumMap<>(LogLevel.class);
        this.nativeToSystem = new HashMap<>();
    }

    /**
     * add log level map
     *
     * @param system      LogLevel
     * @param nativeLevel log level for log4j2 or logback
     */
    public void map(LogLevel system, T nativeLevel) {
        this.systemToNative.putIfAbsent(system, nativeLevel);
        this.nativeToSystem.putIfAbsent(nativeLevel, system);
    }

    public LogLevel convertNativeToSystem(T level) {
        return this.nativeToSystem.get(level);
    }

    public T convertSystemToNative(LogLevel level) {
        return this.systemToNative.get(level);
    }

    public Set<LogLevel> getSupported() {
        return new LinkedHashSet<>(this.nativeToSystem.values());
    }

}
