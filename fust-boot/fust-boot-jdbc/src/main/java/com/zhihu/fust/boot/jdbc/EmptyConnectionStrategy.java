package com.zhihu.fust.boot.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import com.zhihu.fust.spring.jdbc.ConnectionStrategy;

public final class EmptyConnectionStrategy implements ConnectionStrategy {
    static final EmptyConnectionStrategy INSTANCE = new EmptyConnectionStrategy();

    @Override
    public Connection getConnection(String sql) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Connection getMasterConnection() throws SQLException {
        throw new UnsupportedOperationException();
    }

}
