package com.zhihu.fust.spring.redis.common;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;

public class SimpleSetOpsWithExpire<K, V> {

    private RedisTemplate<K, V> redisTemplate;

    public SimpleSetOpsWithExpire(
            RedisTemplate<K, V> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addWithExpire(long timeout, TimeUnit unit, K key, V... values) {
        redisTemplate.opsForSet().add(key, values);
        redisTemplate.expire(key, timeout, unit);
    }

    public void add(K key, V... values) {
        redisTemplate.opsForSet().add(key, values);
    }

    @Nullable
    public Set<V> members(K key) {
        return redisTemplate.opsForSet().members(key);
    }

}
