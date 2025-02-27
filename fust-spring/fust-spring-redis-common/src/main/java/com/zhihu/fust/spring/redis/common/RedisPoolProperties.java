package com.zhihu.fust.spring.redis.common;

/**
 * 仅支持 jedis
 */
public final class RedisPoolProperties {
    private Integer maxIdle;
    /**
     * max wait time(millisecond)
     */
    private Integer maxWait;
    private Integer maxTotal;
    private Integer minIdle;

    public RedisPoolProperties() {
        maxIdle = 8;
        maxWait = 60_000;
        maxTotal = 8;
        minIdle = 4;
    }

    public Integer getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(Integer maxIdle) {
        this.maxIdle = maxIdle;
    }

    public Integer getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(Integer maxWait) {
        this.maxWait = maxWait;
    }

    public Integer getMaxTotal() {
        return maxTotal;
    }

    public void setMaxActive(Integer maxActive) {
        this.maxTotal = maxActive;
    }

    public Integer getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(Integer minIdle) {
        this.minIdle = minIdle;
    }
}
