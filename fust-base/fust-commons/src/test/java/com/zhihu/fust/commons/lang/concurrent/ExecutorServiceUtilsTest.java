package com.zhihu.fust.commons.lang.concurrent;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;

import static org.junit.jupiter.api.Assertions.*;

class ExecutorServiceUtilsTest {

    @Test
    void testNewFixedThreadPool() {
        ExecutorService executorService = ExecutorServiceUtils.newFixedThreadPool(2, "test-pool-%d");
        assertNotNull(executorService);
        assertFalse(executorService.isShutdown());

        assertThrows(IllegalArgumentException.class, () ->
                ExecutorServiceUtils.newFixedThreadPool(0, "test-pool-%d")
        );
    }

    @Test
    void testNewSingleThreadExecutor() {
        ExecutorService executorService = ExecutorServiceUtils.newSingleThreadExecutor("test-single-pool-%d");
        assertNotNull(executorService);
        assertFalse(executorService.isShutdown());
    }

    @Test
    void testNewCachedThreadPool() {
        ExecutorService executorService = ExecutorServiceUtils.newCachedThreadPool(1, 2, "test-cached-pool-%d");
        assertNotNull(executorService);
        assertFalse(executorService.isShutdown());

        executorService = ExecutorServiceUtils.newCachedThreadPool(1, 2, Duration.ofSeconds(30), "test-cached-pool-%d", new SynchronousQueue<>());
        assertNotNull(executorService);
        assertFalse(executorService.isShutdown());

        assertThrows(IllegalArgumentException.class, () ->
                ExecutorServiceUtils.newCachedThreadPool(0, 2, "test-cached-pool-%d")
        );

        assertThrows(IllegalArgumentException.class, () ->
                ExecutorServiceUtils.newCachedThreadPool(1, 0, "test-cached-pool-%d")
        );
    }
}