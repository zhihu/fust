package com.zhihu.fust.spring.redis.lettuce;

import com.zhihu.fust.commons.lang.StringUtils;
import com.zhihu.fust.spring.redis.common.DefaultRedisNodeProperties;
import com.zhihu.fust.spring.redis.common.DefaultRedisProperties;
import com.zhihu.fust.spring.redis.common.RedisFactoryConfig;
import com.zhihu.fust.spring.redis.common.RedisNodeTypeEnum;
import com.zhihu.fust.spring.redis.lettuce.internal.DefaultRedisMasterReplicaNode;
import com.zhihu.fust.telemetry.lettuce.LettuceTracing;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.models.role.RedisInstance;
import io.lettuce.core.protocol.ProtocolVersion;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.EpollProvider;
import io.lettuce.core.resource.KqueueProvider;
import io.lettuce.core.resource.NettyCustomizer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.epoll.EpollChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.*;

import static io.lettuce.core.ReadFrom.*;

public class LettuceConfigFactory implements RedisFactoryConfig {
    private static final Logger log = LoggerFactory.getLogger(LettuceConfigFactory.class);
    private List<DefaultRedisProperties> defaultRedisProperties;
    private Map<String, LettuceConnectionFactory> connectionFactoryMap;

    private LettuceConnectionFactory defaultConnectionFactory;
    private static final SocketOptions.KeepAliveOptions DEFAULT_KEEP_ALIVE_OPTIONS =
            SocketOptions.KeepAliveOptions.builder()
                    .enable(true)
                    .idle(Duration.ofSeconds(5))
                    .interval(Duration.ofSeconds(2))
                    .count(3)
                    .build();

    public LettuceConfigFactory(List<DefaultRedisProperties> propertiesList) {
        this.defaultRedisProperties = propertiesList;
        this.connectionFactoryMap = new HashMap<>();
        this.init();
    }

    private ReadFrom getReadFrom(String name) {
        if (StringUtils.isEmpty(name)) {
            return MASTER_PREFERRED;
        }

        if ("master".equalsIgnoreCase(name)) {
            return MASTER;
        }

        if ("masterPreferred".equalsIgnoreCase(name)) {
            return MASTER_PREFERRED;
        }

        if ("slave".equalsIgnoreCase(name)) {
            return REPLICA;
        }

        if ("REPLICA".equalsIgnoreCase(name)) {
            return REPLICA;
        }

        if ("slavePreferred".equalsIgnoreCase(name)) {
            return REPLICA_PREFERRED;
        }

        if ("replicaPreferred".equalsIgnoreCase(name)) {
            return REPLICA_PREFERRED;
        }

        if ("nearest".equalsIgnoreCase(name) || "LowestLatency".equalsIgnoreCase(name)) {
            return LOWEST_LATENCY;
        }

        return MASTER_PREFERRED;
    }

    private void init() {
        for (DefaultRedisProperties redisProperties : defaultRedisProperties) {
            Optional<DefaultRedisNodeProperties> masterNodeOpt = redisProperties.getNodes().stream()
                    .filter(DefaultRedisNodeProperties::isMaster)
                    .findFirst();

            if (!masterNodeOpt.isPresent()) {
                throw new IllegalArgumentException("no lettuce master config find!");
            }

            ClientResources.Builder builder = ClientResources.builder();
            builder.tracing(new LettuceTracing());
            int tcpUserTimeout = redisProperties.getTcpUserTimeout();
            boolean tcpUserTimeoutAvailable = EpollProvider.isAvailable() && tcpUserTimeout > 0;
            if (tcpUserTimeoutAvailable) {
                // add a log
                log.info("cfgName|{} Enable TCP_USER_TIMEOUT|{} ms", redisProperties.getName(), tcpUserTimeout);
            }
            ClientResources clientResources = builder.nettyCustomizer(new NettyCustomizer() {
                @Override
                public void afterBootstrapInitialized(Bootstrap bootstrap) {
                    // no epoll in mac, so this is only for linux
                    if (tcpUserTimeoutAvailable) {
                        // TCP_USER_TIMEOUT >= TCP_KEEPIDLE + TCP_KEEPINTVL * TCP_KEEPCNT
                        // https://blog.cloudflare.com/when-tcp-sockets-refuse-to-die/
                        bootstrap.option(EpollChannelOption.TCP_USER_TIMEOUT, tcpUserTimeout);
                    }
                }
            }).build();

            boolean enableKeepAlive = redisProperties.isEnableKeepAlive();
            SocketOptions.Builder socketOptionsBuilder = SocketOptions.builder()
                    .connectTimeout(Duration.ofMillis(
                            redisProperties.getConnectionTimeout()));
            if (enableKeepAlive && !KqueueProvider.isAvailable()) {
                // 注意：mac 系统上, lettuce 不支持 keep-alive，keep-alive 主要在 linux 上有用
                SocketOptions.KeepAliveOptions keepAlive = DEFAULT_KEEP_ALIVE_OPTIONS;
                DefaultRedisProperties.KeepAliveOptions ginKeepAliveOptions =
                        redisProperties.getKeepAliveOptions();
                if (ginKeepAliveOptions != null) {
                    keepAlive = SocketOptions.KeepAliveOptions.builder()
                            .enable(true)
                            // 没有数据发送的话，多久后发送探测分组
                            .idle(Duration.ofMillis(
                                    ginKeepAliveOptions.getIdle()))
                            // 前后两次探测之间的时间间隔
                            .interval(Duration.ofMillis(
                                    ginKeepAliveOptions.getInterval()))
                            // 重试次数
                            .count(ginKeepAliveOptions.getCount())
                            .build();
                }
                socketOptionsBuilder.keepAlive(keepAlive);
                log.info("cfgName|{} Add keepAlive, idle|{}s interval|{}s count|{}", redisProperties.getName(),
                        keepAlive.getIdle().getSeconds(), keepAlive.getInterval().getSeconds(),
                        keepAlive.getCount());
            }

            TimeoutOptions timeoutOptions = TimeoutOptions.builder()
                    .fixedTimeout(Duration.ofMillis(
                            redisProperties.getCommandTimeout()))
                    .build();

            ClientOptions clientOptions = ClientOptions.builder()
                    .timeoutOptions(timeoutOptions)
                    .socketOptions(socketOptionsBuilder.build())
                    .protocolVersion(ProtocolVersion.RESP2)
                    .build();
            ReadFrom readFrom = getReadFrom(redisProperties.getReadFrom());
            LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                    .clientOptions(clientOptions)
                    .commandTimeout(
                            Duration.ofMillis(
                                    redisProperties.getCommandTimeout()))
                    .clientResources(
                            clientResources)
                    .readFrom(readFrom)
                    .build();

            MasterReplicaRedisConnectionFactory
                    factory = new MasterReplicaRedisConnectionFactory(redisProperties.getName(),
                    clientConfig);
            factory.setEagerInitialization(redisProperties.isEagerInitialization());
            for (DefaultRedisNodeProperties p : redisProperties.getNodes()) {
                RedisInstance.Role role = p.isMaster() ? RedisInstance.Role.MASTER : RedisInstance.Role.REPLICA;
                DefaultRedisMasterReplicaNode node = new DefaultRedisMasterReplicaNode(p.getHost(), p.getPort(),
                        p.getPassword(), role);
                factory.addNode(node);
            }

            // 默认 factory 会通过 @Bean 方式被执行 afterPropertiesSet， 其他的 factory 不会
            // 如果存在多 lettuce 情况下，可能产生只有默认的初始化，其他的 factory 没有初始化，导致 lettuce 连接创建失败。
            // 对 factory 初始化，避免 lettuce 连接可能创建失败的问题
            factory.afterPropertiesSet(); // init factory

            if (redisProperties.isEagerInitialization()) {
                doPing(factory);
            }

            // 默认 lettuce 实例
            if (redisProperties.isDefaultConnection()) {
                defaultConnectionFactory = factory;
            }
            connectionFactoryMap.put(redisProperties.getName(), factory);
        }

        if (CollectionUtils.isEmpty(connectionFactoryMap)) {
            return;
        }

        // 没有配置默认连接
        if (defaultConnectionFactory == null) {
            Collection<LettuceConnectionFactory> values = connectionFactoryMap.values();
            defaultConnectionFactory = values.stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "not found connection factory"));
            if (values.size() > 1) {
                // 配置了多个 lettuce 但没有指定默认连接，随便取一个
                log.warn("multiple lettuce find, use|{}  as default!", defaultConnectionFactory.getHostName());
            }
        }
    }

    private void doPing(MasterReplicaRedisConnectionFactory factory) {
        // do ping once
        try {
            factory.getConnection().ping();
        } catch (Exception e) {
            log.warn("lettuce ping error! cfgName|{}", factory.getName(), e);
        }
    }

    @Override
    public RedisConnectionFactory getDefault() {
        return defaultConnectionFactory;
    }

    @Override
    public RedisConnectionFactory get(String name) {
        RedisConnectionFactory pool = connectionFactoryMap.get(name);
        if (pool == null) {
            throw new IllegalStateException(name + " for lettuce not found!");
        }
        return pool;
    }

    @Override
    public RedisConnectionFactory get(String name, RedisNodeTypeEnum type) {
        // ignore type, lettuce support ReadFrom config
        return get(name);
    }
}
