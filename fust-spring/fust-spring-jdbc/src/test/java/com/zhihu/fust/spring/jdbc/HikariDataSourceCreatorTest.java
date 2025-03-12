package com.zhihu.fust.spring.jdbc;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class HikariDataSourceCreatorTest {

    private HikariConfig config;
    private HikariDataSourceCreator creator;

    @BeforeEach
    void setUp() {
        config = mock(HikariConfig.class);
        when(config.getJdbcUrl()).thenReturn("jdbc:h2:mem:test");
        when(config.getConnectionTimeout()).thenReturn(30000L);
        creator = new HikariDataSourceCreator(config);
    }

    @Test
    void testGetNowWithInvalidConfig() {
        when(config.getJdbcUrl()).thenReturn(null);
        assertThrows(Exception.class, () -> creator.getNow());
    }

    @Test
    void testGetWithRetryMaxRetries() throws ExecutionException, InterruptedException, TimeoutException {
        ExecutorService executorService = mock(ExecutorService.class);
        Future<HikariDataSource> future = mock(Future.class);
        when(future.get(anyLong(), any(TimeUnit.class))).thenThrow(new TimeoutException());
        when(executorService.submit(any(Callable.class))).thenReturn(future);
        HikariDataSourceCreator creatorWithMockExecutor = new HikariDataSourceCreator(config) {
            ExecutorService getExecutorService() {
                return executorService;
            }
        };

        assertThrows(IllegalStateException.class, creatorWithMockExecutor::getWithRetry);
    }
}