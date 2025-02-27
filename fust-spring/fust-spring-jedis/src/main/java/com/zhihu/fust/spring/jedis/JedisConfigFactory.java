package com.zhihu.fust.spring.jedis;

import com.zhihu.fust.spring.redis.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPoolConfig;

import java.util.*;

public class JedisConfigFactory implements RedisFactoryConfig {
    private static final Logger log = LoggerFactory.getLogger(JedisConfigFactory.class);
    private final List<DefaultRedisProperties> defaultRedisProperties;
    private final Map<String, JedisConnectionFactory> masterFactories;
    private final Map<String, JedisConnectionFactory> replicaFactories;

    private JedisConnectionFactory defaultConnectionFactory;

    public JedisConfigFactory(List<DefaultRedisProperties> propertiesList) {
        this.defaultRedisProperties = propertiesList;
        this.masterFactories = new HashMap<>();
        this.replicaFactories = new HashMap<>();
        this.init();
    }

    private void init() {
        for (DefaultRedisProperties redisProperties : defaultRedisProperties) {
            Optional<DefaultRedisNodeProperties> masterNodeOpt = redisProperties.getNodes().stream()
                    .filter(DefaultRedisNodeProperties::isMaster)
                    .findFirst();

            if (!masterNodeOpt.isPresent()) {
                throw new IllegalArgumentException("no redis master config find!");
            }

            JedisConnectionFactory masterFactory = null;
            for (DefaultRedisNodeProperties p : redisProperties.getNodes()) {
                RedisNodeTypeEnum type = p.isMaster() ? RedisNodeTypeEnum.MASTER : RedisNodeTypeEnum.REPLICA;
                TracingJedisConnectionFactory connectionFactory = new TracingJedisConnectionFactory(p);
                connectionFactory.setPoolConfig(createJedisPool(redisProperties.getPool()));
                if (type == RedisNodeTypeEnum.MASTER) {
                    masterFactory = connectionFactory;
                    masterFactories.put(redisProperties.getName(), connectionFactory);
                } else {
                    replicaFactories.put(redisProperties.getName(), connectionFactory);
                }
                connectionFactory.afterPropertiesSet();
            }

            // 默认 lettuce 实例
            if (redisProperties.isDefaultConnection()) {
                defaultConnectionFactory = masterFactory;
            }
        }

        // not find default connection factory, use first master connection factory
        if (defaultConnectionFactory == null) {
            Collection<JedisConnectionFactory> values = masterFactories.values();
            defaultConnectionFactory = values.stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "not found connection factory"));
            if (values.size() > 1) {
                // warn the config is not very good
                log.warn("multiple lettuce find, use|{}  as default!", defaultConnectionFactory.getHostName());
            }
        }
    }

    public JedisPoolConfig createJedisPool(RedisPoolProperties pool) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        if (pool == null) {
            // use default pool properties
            pool = new RedisPoolProperties();
        }
        jedisPoolConfig.setMaxIdle(pool.getMaxIdle());
        jedisPoolConfig.setMaxTotal(pool.getMaxTotal());
        jedisPoolConfig.setMaxWaitMillis(pool.getMaxWait());
        jedisPoolConfig.setMinIdle(pool.getMinIdle());
        return jedisPoolConfig;
    }


    @Override
    public RedisConnectionFactory getDefault() {
        return defaultConnectionFactory;
    }

    @Override
    public RedisConnectionFactory get(String name) {
        return get(name, RedisNodeTypeEnum.MASTER);
    }


    public RedisConnectionFactory get(String name, RedisNodeTypeEnum type) {
        RedisConnectionFactory factory;
        if (type == RedisNodeTypeEnum.MASTER) {
            factory = masterFactories.get(name);
        } else {
            factory = replicaFactories.get(name);
        }
        Objects.requireNonNull(factory, "no master connection factory find! name=" + name + ", type=" + type);
        return factory;
    }
}
