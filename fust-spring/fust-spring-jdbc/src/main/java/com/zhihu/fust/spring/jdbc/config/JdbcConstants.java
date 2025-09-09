package com.zhihu.fust.spring.jdbc.config;

public abstract class JdbcConstants {
    public static final String MASTER = "master";
    public static final String REPLICA = "replica";
    
    /**
     * jdbc:mysql://{host}:{port}/{database}
     */
    public static final String MYSQL_JDBC_URL_FMT = "jdbc:mysql://%s:%d/%s";
    
    /**
     * jdbc:postgresql://{host}:{port}/{database}
     */
    public static final String POSTGRESQL_JDBC_URL_FMT = "jdbc:postgresql://%s:%d/%s";
    
    // Keep backward compatibility
    public static final String JDBC_URL_FMT = MYSQL_JDBC_URL_FMT;

    public static final String MYSQL_JDBC_DRIVER = "com.mysql.jdbc.Driver";
    public static final String POSTGRESQL_JDBC_DRIVER = "org.postgresql.Driver";
    
    // Keep backward compatibility
    public static final String JDBC_DRIVER = MYSQL_JDBC_DRIVER;
}
