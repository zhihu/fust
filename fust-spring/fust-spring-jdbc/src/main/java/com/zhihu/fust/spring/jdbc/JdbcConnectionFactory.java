package com.zhihu.fust.spring.jdbc;

public class JdbcConnectionFactory {
    private final ConnectionStrategy strategy;

    private boolean directMode;

    public JdbcConnectionFactory(ConnectionStrategy strategy) {
        this.strategy = strategy;
    }

    public void setDirectMode(boolean directMode) {
        this.directMode = directMode;
    }

    public JdbcConnectionWrapper createConnection() {
        return new JdbcConnectionWrapper(strategy, directMode);
    }

    public ConnectionStrategy getConnectStrategy() {
        return strategy;
    }
}
