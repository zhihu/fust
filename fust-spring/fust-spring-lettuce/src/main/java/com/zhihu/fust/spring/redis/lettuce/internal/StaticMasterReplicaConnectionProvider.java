package com.zhihu.fust.spring.redis.lettuce.internal;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.springframework.data.redis.connection.lettuce.LettuceConnectionProvider;
import org.springframework.lang.Nullable;

import io.lettuce.core.ReadFrom;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection;
import io.lettuce.core.models.role.RedisNodeDescription;

/**
 * @author yanzhuzhu
 * @since 2018/11/23
 */
public class StaticMasterReplicaConnectionProvider implements LettuceConnectionProvider {

    private final RedisClient client;
    private final RedisCodec<?, ?> codec;
    private final Optional<ReadFrom> readFrom;
    private final List<RedisNodeDescription> nodes;

    /**
     * @param client   must not be {@literal null}.
     * @param codec    must not be {@literal null}.
     * @param nodes    must not be {@literal null}.
     * @param readFrom can be {@literal null}.
     */
    public StaticMasterReplicaConnectionProvider(RedisClient client, RedisCodec<?, ?> codec,
                                                 List<RedisNodeDescription> nodes,
                                                 @Nullable ReadFrom readFrom) {

        this.client = client;
        this.codec = codec;
        this.readFrom = Optional.ofNullable(readFrom);
        this.nodes = nodes;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.lettuce.
     * connection.lettuce.LettuceConnectionProvider#getConnection(java.lang.Class)
     */
    @Override
    public <T extends StatefulConnection<?, ?>> T getConnection(Class<T> connectionType) {

        if (StatefulConnection.class.isAssignableFrom(connectionType)) {

            // See https://github.com/lettuce-io/lettuce-core/issues/845 for MasterSlave -> MasterReplica change.
            StatefulRedisMasterReplicaConnection<?, ?> connection = DefaultMasterReplica.connect(client, codec,
                                                                                                 nodes);

            readFrom.ifPresent(connection::setReadFrom);

            return connectionType.cast(connection);
        }

        throw new UnsupportedOperationException(
                String.format("Connection type %s not supported!", connectionType));
    }

    @Override
    public <T extends StatefulConnection<?, ?>> CompletionStage<T> getConnectionAsync(Class<T> connectionType) {
        if (StatefulConnection.class.isAssignableFrom(connectionType)) {

            // See https://github.com/lettuce-io/lettuce-core/issues/845 for MasterSlave -> MasterReplica change.
            CompletableFuture<? extends StatefulRedisMasterReplicaConnection<?, ?>> connection =
                    DefaultMasterReplica
                            .connectAsync(client, codec, nodes);

            connection.thenApply(it -> {

                readFrom.ifPresent(readFrom -> it.setReadFrom(readFrom));
                return connectionType.cast(connection);
            });
        }

        throw new UnsupportedOperationException(
                String.format("Connection type %s not supported!", connectionType));
    }

    @Override
    public void release(StatefulConnection<?, ?> connection) {
        FutureUtils.join(releaseAsync(connection));
    }

    @Override
    public CompletableFuture<Void> releaseAsync(StatefulConnection<?, ?> connection) {
        return connection.closeAsync();
    }
}
