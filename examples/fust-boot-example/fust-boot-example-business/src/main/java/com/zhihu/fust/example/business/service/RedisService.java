package com.zhihu.fust.example.business.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;


    public void cacheHello(String name, String time) {
        redisTemplate.opsForValue().set(name, time);
    }

    public String getHelloTime(String name) {
        return redisTemplate.opsForValue().get(name);
    }

    /**
     * test lettuce
     */
    public void test() {
        redisTemplate.opsForValue().set("t1", "test1");
        var s = redisTemplate.opsForValue().get("t1");
        log.info(s);
    }

}
