package com.zhihu.fust.spring.redis.common;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

public class DefaultRawRedisTemplate extends RedisTemplate<String, byte[]> {
    public DefaultRawRedisTemplate() {
        setKeySerializer(new StringRedisSerializer());
        setValueSerializer(new DefaultRawRedisSerializer());
    }

    public DefaultRawRedisTemplate(RedisConnectionFactory connectionFactory) {
        this();
        setConnectionFactory(connectionFactory);
        afterPropertiesSet();
    }
}
