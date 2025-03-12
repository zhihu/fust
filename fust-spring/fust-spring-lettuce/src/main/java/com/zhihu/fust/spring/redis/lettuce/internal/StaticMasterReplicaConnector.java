package com.zhihu.fust.spring.redis.lettuce.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection;
import io.lettuce.core.models.role.RedisNodeDescription;
import reactor.core.publisher.Mono;

public class StaticMasterReplicaConnector<K, V> {
    private final RedisClient redisClient;
    private final RedisCodec<K, V> codec;
    private final List<RedisNodeDescription> redisURIs;

    StaticMasterReplicaConnector(RedisClient redisClient, RedisCodec<K, V> codec,
                                 List<RedisNodeDescription> redisURIs) {
        this.redisClient = redisClient;
        this.codec = codec;
        this.redisURIs = redisURIs;
    }

    public CompletableFuture<StatefulRedisMasterReplicaConnection<K, V>> connectAsync() {

        Map<RedisURI, StatefulRedisConnection<K, V>> initialConnections = new HashMap<>();

        RedisURI seedNode = redisURIs.iterator().next().getUri();

        DefaultLettuceConnectionProvider<K, V> connectionProvider = new DefaultLettuceConnectionProvider<>(
                redisClient, codec,
                seedNode, initialConnections);

        return initializeConnection(codec, seedNode, connectionProvider, redisURIs).toFuture();
    }

    private Mono<StatefulRedisMasterReplicaConnection<K, V>> initializeConnection(
            RedisCodec<K, V> codec, RedisURI seedNode,
            DefaultLettuceConnectionProvider<K, V> connectionProvider, List<RedisNodeDescription> nodes) {

        connectionProvider.setKnownNodes(nodes);

        DefaultMasterReplicaChannelWriter
                channelWriter = new DefaultMasterReplicaChannelWriter(connectionProvider,
                                                                      redisClient.getResources());

        DefaultStatefulRedisMasterReplicaConnectionImpl<K, V> connection =
                new DefaultStatefulRedisMasterReplicaConnectionImpl<>(channelWriter,
                                                                      codec, seedNode.getTimeout());
        connection.setOptions(redisClient.getOptions());

        return Mono.just(connection);
    }
}
