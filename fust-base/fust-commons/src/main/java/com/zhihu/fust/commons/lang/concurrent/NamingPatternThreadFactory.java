package com.zhihu.fust.commons.lang.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;

public final class NamingPatternThreadFactory implements ThreadFactory {
    private final AtomicLong threadCounter;

    private final ThreadFactory wrappedFactory;

    /**
     * Stores the naming pattern for newly created threads.
     */
    private final String namingPattern;
    private final Boolean daemon;

    private NamingPatternThreadFactory(String namingPattern, Boolean daemon) {
        this.namingPattern = namingPattern;
        wrappedFactory = Executors.defaultThreadFactory();
        threadCounter = new AtomicLong();
        this.daemon = daemon;
    }

    public static NamingPatternThreadFactory of(String namingPattern) {
        return new NamingPatternThreadFactory(namingPattern, null);
    }

    public static NamingPatternThreadFactory of(String namingPattern, boolean daemon) {
        return new NamingPatternThreadFactory(namingPattern, daemon);
    }

    @Override
    public Thread newThread(@Nonnull Runnable runnable) {
        final Thread thread = wrappedFactory.newThread(runnable);
        if (namingPattern != null) {
            final Long count = threadCounter.incrementAndGet();
            thread.setName(String.format(namingPattern, count));
        }
        if (daemon != null) {
            thread.setDaemon(daemon);
        }
        return thread;
    }
}
