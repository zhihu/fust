package com.zhihu.fust.spring.redis.common;

import com.zhihu.fust.core.env.Env;

public class DefaultRedisConfigFileProvider implements RedisConfigFileProvider {
    @Override
    public String getRedisConfigFile() {
        String envName = Env.getName().toLowerCase();
        return String.format("classpath:redis-%s.json", envName);
    }
}
