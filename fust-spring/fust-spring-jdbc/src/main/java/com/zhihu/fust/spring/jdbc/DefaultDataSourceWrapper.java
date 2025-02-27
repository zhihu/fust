package com.zhihu.fust.spring.jdbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * wrap data source
 */
public class DefaultDataSourceWrapper extends WrapperAdapter implements DataSource {

    private final JdbcConnectionFactory factory;

    private PrintWriter printWriter = new PrintWriter(System.out);

    public DefaultDataSourceWrapper(JdbcConnectionFactory factory) {
        this.factory = factory;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return factory.createConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return printWriter;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        printWriter = out;
    }

    @Override
    public final int getLoginTimeout() throws SQLException {
        throw new SQLFeatureNotSupportedException("unsupported getLoginTimeout()");
    }

    @Override
    public final void setLoginTimeout(final int seconds) throws SQLException {
        throw new SQLFeatureNotSupportedException("unsupported setLoginTimeout(int seconds)");
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("unsupported getParentLogger");
    }

    public ConnectionStrategy getConnectStrategy() {
        return factory.getConnectStrategy();
    }
}
