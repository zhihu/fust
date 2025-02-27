package com.zhihu.fust.spring.redis.common;

import java.util.List;

public interface RedisResourceDiscover {
    List<DefaultRedisNodeProperties> discover(String name);
}
