package com.zhihu.fust.spring.jedis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPool;

public class TracingJedisPool extends JedisPool {
    public TracingJedisPool(GenericObjectPoolConfig<Jedis> poolConfig, HostAndPort hostAndPort, JedisClientConfig clientConfig) {
        super(poolConfig, new TracingJedisFactory(hostAndPort, clientConfig));
    }
}
