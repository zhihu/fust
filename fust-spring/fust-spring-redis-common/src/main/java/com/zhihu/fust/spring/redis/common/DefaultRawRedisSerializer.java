package com.zhihu.fust.spring.redis.common;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * accept bytes for serialize
 * User: yzz
 * @since 2019-03-30 10:09
 */
public class DefaultRawRedisSerializer implements RedisSerializer<byte[]> {
    @Override
    public byte[] serialize(byte[] bytes) throws SerializationException {
        return bytes;
    }

    @Override
    public byte[] deserialize(byte[] bytes) throws SerializationException {
        return bytes;
    }
}
