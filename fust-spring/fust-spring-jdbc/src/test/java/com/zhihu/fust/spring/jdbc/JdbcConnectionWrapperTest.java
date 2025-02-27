package com.zhihu.fust.spring.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class JdbcConnectionWrapperTest {

    private ConnectionStrategy mockStrategy;
    private Connection mockConnection;
    private JdbcConnectionWrapper directWrapper;
    private JdbcConnectionWrapper parentWrapper;

    @BeforeEach
    void setUp() throws SQLException {
        mockStrategy = mock(ConnectionStrategy.class);
        mockConnection = mock(Connection.class);
        when(mockStrategy.getConnection(anyString())).thenReturn(mockConnection);
        when(mockStrategy.getMasterConnection()).thenReturn(mockConnection);

        directWrapper = new JdbcConnectionWrapper(mockStrategy, true);
        parentWrapper = new JdbcConnectionWrapper(mockStrategy, false);
    }

    @Test
    void testPrepareStatement() throws SQLException {
        String sql = "SELECT * FROM table";
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(sql)).thenReturn(mockStatement);

        PreparedStatement statement = directWrapper.prepareStatement(sql);
        assertNotNull(statement);
        verify(mockConnection, times(1)).prepareStatement(sql);
    }

    @Test
    void testPrepareStatementWithResultSetType() throws SQLException {
        String sql = "SELECT * FROM table";
        int resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
        int resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(sql, resultSetType, resultSetConcurrency)).thenReturn(mockStatement);

        PreparedStatement statement = directWrapper.prepareStatement(sql, resultSetType, resultSetConcurrency);
        assertNotNull(statement);
        verify(mockConnection, times(1)).prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Test
    void testGetMetaData() throws SQLException {
        DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);

        DatabaseMetaData metaData = directWrapper.getMetaData();
        assertNotNull(metaData);
        verify(mockConnection, times(1)).getMetaData();
    }

}