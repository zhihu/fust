package com.zhihu.fust.spring.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zhihu.fust.core.env.Env;
import com.zhihu.fust.spring.jdbc.config.DataSourceProperties;
import com.zhihu.fust.spring.jdbc.config.DatabaseProperties;
import com.zhihu.fust.telemetry.mysql.TracingQueryInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.*;

public class DataSourceAdapter {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceAdapter.class);
    private final DataSource master;
    private final List<DataSource> slaves;
    private final List<DataSourceProperties> slavePropertiesList;
    private final Random random = new Random();
    private final String name;
    private final boolean masterOnly;
    private boolean defaultDb;
    private static final String DEFAULT_TRACER = TracingQueryInterceptor.class.getName();

    public boolean isDefaultDb() {
        return defaultDb;
    }

    public void setDefaultDb() {
        defaultDb = true;
    }

    /**
     * 通过 data source 直接构建
     *
     * @param name   dbName
     * @param master master DataSource
     */
    public DataSourceAdapter(String name, DataSource master) {
        this(name, master, Collections.singletonList(master), true);
    }

    /**
     * 通过 data source 直接构建
     *
     * @param name       dbName
     * @param master     master DataSource
     * @param slaves     slave DataSources
     * @param masterOnly always use master data source
     */
    public DataSourceAdapter(String name, DataSource master, List<DataSource> slaves, boolean masterOnly) {
        this.name = name;
        this.slavePropertiesList = new ArrayList<>();
        this.master = master;
        this.slaves = slaves;
        this.masterOnly = masterOnly;
    }

    /**
     * 通过 DatabaseProperties 构建
     *
     * @param properties 数据库配置属性
     */
    public DataSourceAdapter(DatabaseProperties properties) {
        this.slaves = new ArrayList<>();
        this.slavePropertiesList = new ArrayList<>();
        DataSource localMaster = null;
        List<DataSourceProperties> propertiesList = properties.getDataSourcePropertiesList();
        for (DataSourceProperties p : propertiesList) {
            if (p.isMaster()) {
                if (localMaster != null) {
                    logger.error("multi master find, new master|{}", p.getName());
                    throw new IllegalStateException("Not support multi master");
                }
                localMaster = createDataSource(properties, p);
            } else {
                slavePropertiesList.add(p);
                slaves.add(createDataSource(properties, p));
            }
        }
        this.name = properties.getName();
        this.master = localMaster;
        this.masterOnly = properties.isMasterOnly();
    }

    public String getName() {
        return name;
    }

    private static DataSource createDataSource(DatabaseProperties db, DataSourceProperties ds) {
        HikariConfig config = new HikariConfig();

        config.setMinimumIdle(db.getMinIdle());
        config.setMaximumPoolSize(db.getMaxPoolSize());
        config.addDataSourceProperty("useSSL", String.valueOf(db.isUseSSL()));

        config.setJdbcUrl(ds.getUrl());
        config.setUsername(ds.getUsername());
        config.setPassword(ds.getPassword());

        config.addDataSourceProperty("characterEncoding", "utf8");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        // process 0000-00-00 00:00:00 to null in java
        config.addDataSourceProperty("zeroDateTimeBehavior", "convertToNull");
        config.addDataSourceProperty("serverTimezone", String.valueOf(ZoneId.systemDefault().getId()));
        config.setConnectionInitSql("SET NAMES utf8mb4");

        // 设置数据连接超时，默认值为 30s
        Optional.ofNullable(db.getConnectionTimeoutMs())
                .ifPresent(config::setConnectionTimeout);

        config.addDataSourceProperty("queryInterceptors", DEFAULT_TRACER);

        HikariDataSourceCreator creator = new HikariDataSourceCreator(config);
        if (Env.isProduction()) {
            return creator.getNow();
        }
        return creator.getWithRetry();
    }

    public boolean isMasterOnly() {
        return masterOnly;
    }

    public Connection getMasterConnection() throws SQLException {
        logger.debug("GET master connection from db|{}", name);
        return master.getConnection();
    }

    public Connection getSlaveConnection() throws SQLException {
        if (slaves.isEmpty()) {
            logger.debug("no slave db found, return master connection");
            return getMasterConnection();
        }
        int index = random.nextInt(slaves.size());
        DataSourceProperties p = slavePropertiesList.get(index);
        if (logger.isDebugEnabled()) {
            logger.debug("GET slave connection from db|{}  slave|{}", name, p.getName());
        }
        return slaves.get(index).getConnection();
    }

    public DataSource getMaster() {
        return master;
    }
}
