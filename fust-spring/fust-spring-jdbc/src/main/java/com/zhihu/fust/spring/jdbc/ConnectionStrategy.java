package com.zhihu.fust.spring.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionStrategy {
    Connection getConnection(String sql) throws SQLException;

    Connection getMasterConnection() throws SQLException;

    default String getDatabaseName() {
        return "default";
    }
}
