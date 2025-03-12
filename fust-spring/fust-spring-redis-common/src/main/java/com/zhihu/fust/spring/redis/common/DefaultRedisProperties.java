package com.zhihu.fust.spring.redis.common;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author yanzhuzhu
 * @since 2018/10/25
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultRedisProperties {

    public static class KeepAliveOptions {
        /**
         * 空闲等待时间，空闲超过此时间，发送 keep-alive 包，单位 ms
         */
        private int idle;

        /**
         * 发送 keep-alive 包超时的间隔时间，单位 ms
         */
        private int interval;

        /**
         * 重试次数
         */
        private int count;

        public int getIdle() {
            return idle;
        }

        public void setIdle(int idle) {
            this.idle = idle;
        }

        public int getInterval() {
            return interval;
        }

        public void setInterval(int interval) {
            this.interval = interval;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }


    @JsonProperty("nodes")
    private List<DefaultRedisNodeProperties> nodes;
    private String name;

    /**
     * 默认策略：读主库，仅当主库不可时，读从库
     */
    private String readFrom = "masterPreferred";
    private static final long DEFAULT_COMMAND_TIMEOUT = 500L;
    private static final long DEFAULT_CONNECTION_TIMEOUT = 1000L;

    /**
     * 命令执行超时
     */
    private long commandTimeout = DEFAULT_COMMAND_TIMEOUT;

    /**
     * 连接超时
     */
    private long connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

    /**
     * 预热 lettuce 连接，默认 true
     */
    private boolean eagerInitialization = true;

    /**
     * 获取验证连接时，是否需要验证连接，默认 true
     */
    private boolean validateConnection = true;

    private boolean defaultConnection;

    /**
     * 默认的 TCP_USER_TIMEOUT 时间，单位 ms
     * -1 表示，不开启超时配置
     */
    private int tcpUserTimeout = 60_000;
    /**
     * 是否开启 keep-alive 选项，默认开启
     */
    private boolean enableKeepAlive = true;

    /**
     * keep-alive 选项, lettuce 专用
     */
    private KeepAliveOptions keepAliveOptions;

    /**
     * pool 设置， jedis 专用
     */
    private RedisPoolProperties pool;

    public String getName() {
        return name;
    }

    public String getMasterId() {
        return name + "_" + RedisConstants.PRIMARY;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DefaultRedisNodeProperties> getNodes() {
        return nodes;
    }

    public void setNodes(List<DefaultRedisNodeProperties> nodes) {
        this.nodes = nodes;
    }

    public boolean isDefaultConnection() {
        return defaultConnection;
    }

    public long getCommandTimeout() {
        return commandTimeout;
    }

    public void setCommandTimeout(long commandTimeout) {
        this.commandTimeout = commandTimeout;
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public void setDefaultConnection(boolean defaultConnection) {
        this.defaultConnection = defaultConnection;
    }

    public String getReadFrom() {
        return readFrom;
    }

    public void setReadFrom(String readFrom) {
        this.readFrom = readFrom;
    }

    public void setEagerInitialization(boolean eagerInitialization) {
        this.eagerInitialization = eagerInitialization;
    }

    public boolean isEagerInitialization() {
        return eagerInitialization;
    }

    public int getTcpUserTimeout() {
        return tcpUserTimeout;
    }

    public boolean isEnableKeepAlive() {
        return enableKeepAlive;
    }

    public KeepAliveOptions getKeepAliveOptions() {
        return keepAliveOptions;
    }

    public RedisPoolProperties getPool() {
        return pool;
    }

    /***
     * 如果仅有一个节点，自动设置为 primary 节点
     */
    public void checkPrimary() {
        if (nodes.size() == 1) {
            nodes.get(0).setType(RedisConstants.PRIMARY);
        }
    }
}
