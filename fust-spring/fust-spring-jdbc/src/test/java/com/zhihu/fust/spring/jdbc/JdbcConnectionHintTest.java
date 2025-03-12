package com.zhihu.fust.spring.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JdbcConnectionHintTest {

    @BeforeEach
    void setUp() {
        JdbcConnectionHint.clear();
    }

    @Test
    void testIsMasterTrue() {
        JdbcConnectionHint.setMaster(true);
        assertTrue(JdbcConnectionHint.isMaster());
    }

    @Test
    void testIsMasterFalse() {
        JdbcConnectionHint.setMaster(false);
        assertFalse(JdbcConnectionHint.isMaster());
    }

    @Test
    void testSetMasterTrue() {
        JdbcConnectionHint.setMaster(true);
        assertTrue(JdbcConnectionHint.isMaster());
    }

    @Test
    void testSetMasterFalse() {
        JdbcConnectionHint.setMaster(false);
        assertFalse(JdbcConnectionHint.isMaster());
    }

    @Test
    void testSetDatabaseName() {
        String dbName = "testDb";
        JdbcConnectionHint.setDatabaseName(dbName);
        assertEquals(dbName, JdbcConnectionHint.getDatabaseName());
    }

    @Test
    void testGetDatabaseNameAfterSetting() {
        String dbName = "testDb";
        JdbcConnectionHint.setDatabaseName(dbName);
        assertEquals(dbName, JdbcConnectionHint.getDatabaseName());
    }

    @Test
    void testGetDatabaseNameWhenNotSet() {
        assertNull(JdbcConnectionHint.getDatabaseName());
    }

    @Test
    void testClear() {
        JdbcConnectionHint.setMaster(true);
        JdbcConnectionHint.setDatabaseName("testDb");
        JdbcConnectionHint.clear();
        assertFalse(JdbcConnectionHint.isMaster());
        assertNull(JdbcConnectionHint.getDatabaseName());
    }
}