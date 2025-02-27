package com.zhihu.fust.spring.jdbc.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.CollectionUtils;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DatabaseProperties {

    private static final int DEFAULT_MIN_IDLE_SIZE = 10;
    private static final int DEFAULT_MAX_POOL_SIZE = 30;

    private String name;
    private boolean useSSL = false;
    private int minIdle = DEFAULT_MIN_IDLE_SIZE;
    private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;

    private String addition;
    @JsonProperty("ds")
    private List<DataSourceProperties> dataSourcePropertiesList;

    @JsonProperty("default")
    private boolean defaultDatabase;

    /**
     * default value is 30_000ms (30s)
     * 0 means no timeout
     * min value at least 250ms
     */
    private Integer connectionTimeoutMs;

    /**
     * use master server only
     */
    private boolean masterOnly;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMasterOnly(boolean masterOnly) {
        this.masterOnly = masterOnly;
    }

    public boolean isMasterOnly() {
        return masterOnly;
    }

    public List<DataSourceProperties> getDataSourcePropertiesList() {
        return dataSourcePropertiesList;
    }

    public void setDataSourcePropertiesList(List<DataSourceProperties> dataSourcePropertiesList) {
        this.dataSourcePropertiesList = dataSourcePropertiesList;
    }

    public boolean isDefaultDatabase() {
        return defaultDatabase;
    }

    public void setDefaultDatabase(boolean defaultDatabase) {
        this.defaultDatabase = defaultDatabase;
    }

    public boolean isAutoDiscover() {
        return CollectionUtils.isEmpty(dataSourcePropertiesList);
    }

    public boolean isUseSSL() {
        return useSSL;
    }

    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public Integer getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public void setConnectionTimeoutMs(Integer connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

}
