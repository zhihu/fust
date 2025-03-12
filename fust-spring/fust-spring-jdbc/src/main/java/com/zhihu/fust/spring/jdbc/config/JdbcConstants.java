package com.zhihu.fust.spring.jdbc.config;

public abstract class JdbcConstants {
    public static final String MASTER = "master";
    public static final String REPLICA = "replica";
    /**
     * jdbc:mysql://{host}:{port}/{database}
     */
    public static final String JDBC_URL_FMT = "jdbc:mysql://%s:%d/%s";

    public static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
}
