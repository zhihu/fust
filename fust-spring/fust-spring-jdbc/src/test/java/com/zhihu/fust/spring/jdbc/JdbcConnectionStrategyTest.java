package com.zhihu.fust.spring.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class JdbcConnectionStrategyTest {

    private JdbcConnectionStrategy connectionStrategy;
    private DataSourceAdapter mockAdapter;

    @BeforeEach
    void setUp() {
        mockAdapter = mock(DataSourceAdapter.class);
        connectionStrategy = new JdbcConnectionStrategy("defaultDb", Collections.singletonList(mockAdapter));
    }

    @Test
    void testGetConnectionWithEmptyTargetDataSources() {
        JdbcConnectionStrategy emptyStrategy = new JdbcConnectionStrategy("defaultDb", Collections.emptyList());
        assertThrows(IllegalStateException.class, () -> emptyStrategy.getConnection("SELECT * FROM table"));
    }


    @Test
    void testGetMasterConnectionWithEmptyTargetDataSources() {
        JdbcConnectionStrategy emptyStrategy = new JdbcConnectionStrategy("defaultDb", Collections.emptyList());
        assertThrows(IllegalStateException.class, emptyStrategy::getMasterConnection);
    }

    @Test
    void testGetDatabaseNameWithNonEmptyHint() {
        JdbcConnectionHint.setDatabaseName("testDb");
        assertEquals("testDb", connectionStrategy.getDatabaseName());
        JdbcConnectionHint.clear();
    }

    @Test
    void testGetDatabaseNameWithEmptyHint() {
        assertEquals("defaultDb", connectionStrategy.getDatabaseName());
    }
}