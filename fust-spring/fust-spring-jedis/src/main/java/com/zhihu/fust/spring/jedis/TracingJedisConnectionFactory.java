package com.zhihu.fust.spring.jedis;

import com.zhihu.fust.spring.redis.common.DefaultRedisNodeProperties;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.util.Pool;

import javax.annotation.Nonnull;

public class TracingJedisConnectionFactory extends JedisConnectionFactory {
    private final DefaultRedisNodeProperties nodeProperties;
    private JedisClientConfig clientConfig = DefaultJedisClientConfig.builder().build();

    private final RedisStandaloneConfiguration standaloneConfig;

    public TracingJedisConnectionFactory(DefaultRedisNodeProperties nodeProperties) {
        super(createJedisConfig(nodeProperties));
        this.standaloneConfig = createJedisConfig(nodeProperties);
        this.nodeProperties = nodeProperties;
    }


    @Override
    public void afterPropertiesSet() {
        // create client config for pool
        this.clientConfig = createClientConfig(getDatabase(), standaloneConfig.getUsername(), standaloneConfig.getPassword());
        super.afterPropertiesSet();
    }

    private JedisClientConfig createClientConfig(int database, @Nullable String username, RedisPassword password) {

        DefaultJedisClientConfig.Builder builder = DefaultJedisClientConfig.builder();

        getClientConfiguration().getClientName().ifPresent(builder::clientName);
        builder.connectionTimeoutMillis(Math.toIntExact(getClientConfiguration().getConnectTimeout().toMillis()));
        builder.socketTimeoutMillis(Math.toIntExact(getClientConfiguration().getReadTimeout().toMillis()));

        builder.database(database);

        if (!ObjectUtils.isEmpty(username)) {
            builder.user(username);
        }
        password.toOptional().map(String::new).ifPresent(builder::password);

        if (isUseSsl()) {

            builder.ssl(true);

            getClientConfiguration().getSslSocketFactory().ifPresent(builder::sslSocketFactory);
            getClientConfiguration().getHostnameVerifier().ifPresent(builder::hostnameVerifier);
            getClientConfiguration().getSslParameters().ifPresent(builder::sslParameters);
        }

        return builder.build();
    }

    @Override
    @Nonnull
    protected Jedis fetchJedisConnector() {
        if (getUsePool()) {
            // pool already has proxy jedis
            return super.fetchJedisConnector();
        }

        // create new jedis proxy instance
        Jedis jedis = super.fetchJedisConnector();
        try {
            return TracingProxyFactory.createProxy(jedis, new HostAndPort(nodeProperties.getHost(), nodeProperties.getPort()));
        } catch (Exception e) {
            throw new IllegalStateException("create jedis proxy failed", e);
        }
    }

    @Override
    @Nonnull
    protected Pool<Jedis> createRedisPool() {
        return new TracingJedisPool(getPoolConfig(), new HostAndPort(getHostName(), getPort()), clientConfig);
    }

    private static RedisStandaloneConfiguration createJedisConfig(DefaultRedisNodeProperties node) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(node.getHost());
        config.setPort(node.getPort());
        config.setPassword(RedisPassword.of(node.getPassword()));
        return config;
    }
}
