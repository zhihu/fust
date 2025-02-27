package com.zhihu.fust.spring.redis.common;

import org.springframework.data.redis.connection.RedisConnectionFactory;

public interface RedisFactoryConfig {
    /**
     * get default RedisConnectionFactory
     */
    RedisConnectionFactory getDefault();

    /**
     * lettuce support readFrom
     */
    RedisConnectionFactory get(String name);

    /**
     * jedis not support readFrom, you need explicitly specify the type to get RedisConnectionFactory
     */
    RedisConnectionFactory get(String name, RedisNodeTypeEnum type);
}
