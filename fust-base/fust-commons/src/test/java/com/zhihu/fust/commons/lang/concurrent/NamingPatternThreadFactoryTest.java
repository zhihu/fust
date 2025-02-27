package com.zhihu.fust.commons.lang.concurrent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NamingPatternThreadFactoryTest {

    @Test
    void testThreadNaming() {
        NamingPatternThreadFactory factory = NamingPatternThreadFactory.of("test-thread-%d");
        Thread thread = factory.newThread(() -> {
        });
        assertNotNull(thread);
        assertTrue(thread.getName().startsWith("test-thread-"));
    }

    @Test
    void testDaemonThread() {
        NamingPatternThreadFactory factory = NamingPatternThreadFactory.of("test-thread-%d", true);
        Thread thread = factory.newThread(() -> {
        });
        assertNotNull(thread);
        assertTrue(thread.isDaemon());
    }

    @Test
    void testNonDaemonThread() {
        NamingPatternThreadFactory factory = NamingPatternThreadFactory.of("test-thread-%d", false);
        Thread thread = factory.newThread(() -> {
        });
        assertNotNull(thread);
        assertFalse(thread.isDaemon());
    }
}