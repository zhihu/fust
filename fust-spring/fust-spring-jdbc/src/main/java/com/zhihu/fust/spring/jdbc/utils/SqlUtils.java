package com.zhihu.fust.spring.jdbc.utils;

import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.zhihu.fust.spring.jdbc.JdbcConnectionHint;

public class SqlUtils {
    private static final String UPDATE = "update";
    private static final String INSERT = "insert";
    private static final String TRUNCATE = "truncate";
    private static final String DELETE = "delete";
    private static final String FOR_UPDATE = "for update";

    private static boolean isUpdateSql(String sql) {
        sql = sql.toLowerCase();
        return sql.startsWith(UPDATE) || sql.contains(FOR_UPDATE)
               || sql.contains(INSERT) || sql.contains(DELETE) || sql.contains(TRUNCATE);
    }

    /**
     * hint is master or in transaction auto use master connection
     */
    public static boolean isMaster(String sql) {
        return JdbcConnectionHint.isMaster()
               || TransactionSynchronizationManager.isActualTransactionActive()
               || isUpdateSql(sql);
    }
}
