package com.zhihu.fust.spring.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhihu.fust.commons.lang.StringUtils;
import com.zhihu.fust.spring.jdbc.utils.SqlUtils;

/**
 * 默认连接策略实现
 */
public class JdbcConnectionStrategy implements ConnectionStrategy {

    private static final Logger log = LoggerFactory.getLogger(JdbcConnectionStrategy.class);
    /***
     * 映射的数据源
     * databaseName -> DataSourceAdapter
     */
    private final Map<String, DataSourceAdapter> targetDataSources;

    /***
     * 默认数据库
     */
    private final String defaultName;
    private final boolean empty;

    public JdbcConnectionStrategy(String defaultDbName, List<DataSourceAdapter> adapters) {
        defaultName = defaultDbName;
        targetDataSources = adapters.stream()
                                    .collect(Collectors.toMap(DataSourceAdapter::getName,
                                                              Function.identity(), (x, y) -> x));
        empty = targetDataSources.isEmpty();
        // set default db
        adapters.stream().filter(x -> Objects.equals(defaultDbName, x.getName()))
                .findFirst()
                .ifPresent(DataSourceAdapter::setDefaultDb);
    }

    public Map<String, DataSourceAdapter> getTargetDataSources() {
        return targetDataSources;
    }

    @Override
    public Connection getConnection(String sql) throws SQLException {
        log.debug("get connect by sql|{}", sql);
        if (empty) {
            throw new IllegalStateException("targetDataSources is empty");
        }

        if (SqlUtils.isMaster(sql)) {
            return getMasterConnection();
        }

        String dbName = getDatabaseName();
        DataSourceAdapter sourceAdapter = targetDataSources.get(dbName);

        if (sourceAdapter.isMasterOnly()) {
            return sourceAdapter.getMasterConnection();
        }
        return sourceAdapter.getSlaveConnection();
    }

    @Override
    public Connection getMasterConnection() throws SQLException {
        if (empty) {
            throw new IllegalStateException("targetDataSources is empty");
        }
        String dbName = getDatabaseName();
        return targetDataSources.get(dbName).getMasterConnection();
    }

    @Override
    public String getDatabaseName() {
        String dbName = JdbcConnectionHint.getDatabaseName();
        if (!StringUtils.isEmpty(dbName)) {
            return dbName;
        }
        return defaultName;
    }

}
