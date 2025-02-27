package com.zhihu.fust.commons.lang.concurrent;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.concurrent.*;

public final class ExecutorServiceUtils {

    private static final Duration SECONDS_60 = Duration.ofSeconds(60);

    private ExecutorServiceUtils() {
    }

    /**
     * Creates a thread pool that reuses a fixed number of threads
     * operating off a shared unbounded queue, using the provided
     * ThreadFactory to create new threads when needed.  At any point,
     * at most {@code nThreads} threads will be active processing
     * tasks.  If additional tasks are submitted when all threads are
     * active, they will wait in the queue until a thread is
     * available.  If any thread terminates due to a failure during
     * execution prior to shutdown, a new one will take its place if
     * needed to execute subsequent tasks.  The threads in the pool will
     * exist until it is explicitly {@link ExecutorService#shutdown
     * shutdown}.
     *
     * @param nThreads the number of threads in the pool
     * @return the newly created thread pool
     * @throws NullPointerException     if threadFactory is null
     * @throws IllegalArgumentException if {@code nThreads <= 0}
     */
    public static ExecutorService newFixedThreadPool(int nThreads, @Nullable String namingPattern) {
        if (nThreads == 0) {
            throw new IllegalArgumentException("nThread must > 0");
        }
        return Executors.newFixedThreadPool(nThreads, NamingPatternThreadFactory.of(namingPattern));
    }

    /**
     * /**
     * Creates an Executor that uses a single worker thread operating
     * off an unbounded queue, and uses the provided ThreadFactory to
     * create a new thread when needed. Unlike the otherwise
     * equivalent {@code newFixedThreadPool(1, threadFactory)} the
     * returned executor is guaranteed not to be reconfigurable to use
     * additional threads.
     *
     * @param namingPattern thread name pattern, usually like "myCustom-pool-%d"
     */
    public static ExecutorService newSingleThreadExecutor(@Nullable String namingPattern) {
        return Executors.newSingleThreadExecutor(NamingPatternThreadFactory.of(namingPattern));
    }

    /**
     * Creates a new {@code ThreadPoolExecutor} with the given initial
     * parameters and default thread factory and rejected execution handler.
     * It may be more convenient to use one of the {@link Executors} factory
     * methods instead of this general purpose constructor.
     *
     * @param corePoolSize    the number of threads to keep in the pool, even
     *                        if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the
     *                        pool
     * @param namingPattern   thread name pattern, usually like "myCustom-pool-%d"
     */
    public static ExecutorService newCachedThreadPool(int corePoolSize,
                                                      int maximumPoolSize,
                                                      @Nullable String namingPattern) {
        if (corePoolSize == 0) {
            throw new IllegalArgumentException("corePoolSize must > 0");
        }
        return newCachedThreadPool(corePoolSize, maximumPoolSize, SECONDS_60,
                namingPattern, new SynchronousQueue<>());
    }

    /**
     * Creates a new {@code ThreadPoolExecutor} with the given initial
     * parameters and default thread factory and rejected execution handler.
     * It may be more convenient to use one of the {@link Executors} factory
     * methods instead of this general purpose constructor.
     *
     * @param corePoolSize    the number of threads to keep in the pool, even
     *                        if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the
     *                        pool
     * @param keepAliveTime   when the number of threads is greater than
     *                        the core, this is the maximum time that excess idle threads
     *                        will wait for new tasks before terminating.
     * @param namingPattern   thread name pattern, usually like "myCustom-pool-%d"
     * @param workQueue       the queue to use for holding tasks before they are
     *                        executed.  This queue will hold only the {@code Runnable}
     *                        tasks submitted by the {@code execute} method.
     */
    public static ExecutorService newCachedThreadPool(int corePoolSize, int maximumPoolSize,
                                                      Duration keepAliveTime,
                                                      @Nullable String namingPattern,
                                                      BlockingQueue<Runnable> workQueue) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                keepAliveTime.getSeconds(), TimeUnit.SECONDS,
                workQueue,
                NamingPatternThreadFactory.of(namingPattern));
    }
}
