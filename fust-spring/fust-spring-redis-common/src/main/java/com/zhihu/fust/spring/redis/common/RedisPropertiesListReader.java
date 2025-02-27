package com.zhihu.fust.spring.redis.common;

import java.util.List;

public interface RedisPropertiesListReader {
    List<DefaultRedisProperties> getRedisPropertiesList(RedisConfigFileProvider redisConfigFileProvider,
                                                        RedisResourceDiscover resourceDiscover);
}
