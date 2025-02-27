package com.zhihu.fust.spring.jdbc;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zhihu.fust.commons.lang.concurrent.ExecutorServiceUtils;

/**
 * for non-production environment, the db connect maybe not stable as production
 * Use this class to retry when in this environment
 */
class HikariDataSourceCreator {

    private static final Logger log = LoggerFactory.getLogger(HikariDataSourceCreator.class);
    private static final int MAX_RETRY_COUNT = 3;
    private static final ExecutorService INIT_EXECUTOR =
            ExecutorServiceUtils.newCachedThreadPool(3, 20, "default-dataSource-init-%d");

    // 配置项
    private final HikariConfig config;

    // 是否成功
    private volatile boolean success;

    private static final String ERROR_MSG = "Init db error, url|{} connectionTimeout|{}";

    HikariDataSourceCreator(HikariConfig config) {
        this.config = config;
    }

    ExecutorService getExecutorService() {
        return INIT_EXECUTOR;
    }

    public HikariDataSource getNow() {
        try {
            return new HikariDataSource(config);
        } catch (Exception e) {
            log.error(ERROR_MSG,
                    config.getJdbcUrl(), config.getConnectionTimeout());
            throw e;
        }
    }

    public HikariDataSource getWithRetry() {
        for (int i = 0; i < MAX_RETRY_COUNT; i++) {
            Future<HikariDataSource> future = getExecutorService().submit(() -> {
                try {
                    log.info("startInitDataSource, url|{} connectionTimeout|{}",
                            config.getJdbcUrl(), config.getConnectionTimeout());
                    HikariDataSource dataSource = new HikariDataSource(config);

                    if (!success) {
                        // 没有设置成功标记，返回，并不关闭
                        return dataSource;
                    }

                    // 如果 future 获取超时，HikariDataSource 也可能线程执行中正常
                    // 后续的线程已经成功创建的 dataSource 需要关闭释放
                    dataSource.close();
                    log.warn("multiCreateDB url|{}", config.getJdbcUrl());
                } catch (Exception e) {
                    log.error(ERROR_MSG, config.getJdbcUrl(), config.getConnectionTimeout());
                }
                return null;
            });

            try {
                HikariDataSource dataSource = future.get(10, TimeUnit.SECONDS);
                if (dataSource != null) {
                    // 设置为成功标记，并返回
                    success = true;
                    return dataSource;
                }
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                // 超时或其他异常，记录下，如果是中断异常则直接中断处理
                log.error("Init db error, url|{} connectionTimeout|{}",
                        config.getJdbcUrl(), config.getConnectionTimeout());
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new IllegalStateException("init db error|{} by 3 times" + config.getJdbcUrl());
    }
}
