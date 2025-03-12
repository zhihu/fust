package com.zhihu.fust.spring.redis.common;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zhihu.fust.commons.lang.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultRedisNodeProperties {

    private boolean defaultConnection;
    private String name;
    private String host;
    private int port;
    private String password = "";
    private long maxWaitMillis = -1L;
    private boolean fairness = true;
    private boolean lifo = true;
    private String evictionPolicyClassName;
    private boolean blockWhenExhausted = true;
    private long timeBetweenEvictionRunsMillis = -1L;
    private String type;

    public DefaultRedisNodeProperties() {
        this.type = RedisConstants.PRIMARY;
    }

    public boolean isMaster() {
        return RedisConstants.PRIMARY.equals(type);
    }

    public boolean isDefaultConnection() {
        return defaultConnection;
    }

    public void setDefaultConnection(boolean defaultConnection) {
        this.defaultConnection = defaultConnection;
    }

    public long getMaxWaitMillis() {
        return maxWaitMillis;
    }

    public void setMaxWaitMillis(long maxWaitMillis) {
        this.maxWaitMillis = maxWaitMillis;
    }

    public String getEvictionPolicyClassName() {
        return evictionPolicyClassName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isAutoDiscover() {
        return StringUtils.isEmpty(host);
    }
}
