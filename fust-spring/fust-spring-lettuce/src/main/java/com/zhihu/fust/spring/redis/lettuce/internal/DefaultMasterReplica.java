package com.zhihu.fust.spring.redis.lettuce.internal;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import io.lettuce.core.ConnectionFuture;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisConnectionException;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.internal.Futures;
import io.lettuce.core.internal.LettuceAssert;
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection;
import io.lettuce.core.models.role.RedisNodeDescription;

public class DefaultMasterReplica {

    public static <K, V> StatefulRedisMasterReplicaConnection<K, V> connect(RedisClient redisClient,
                                                                            RedisCodec<K, V> codec,
                                                                            List<RedisNodeDescription> redisURIs) {
        return getConnection(connectAsyncSentinelOrStaticSetup(redisClient, codec, redisURIs), redisURIs);
    }

    private static <K, V> CompletableFuture<StatefulRedisMasterReplicaConnection<K, V>> connectAsyncSentinelOrStaticSetup(
            RedisClient redisClient, RedisCodec<K, V> codec, List<RedisNodeDescription> redisURIs) {

        LettuceAssert.notNull(redisClient, "RedisClient must not be null");
        LettuceAssert.notNull(codec, "RedisCodec must not be null");
        LettuceAssert.notNull(redisURIs, "RedisURIs must not be null");

        return new StaticMasterReplicaConnector<>(redisClient, codec, redisURIs).connectAsync();
    }

    static <K, V> CompletableFuture<StatefulRedisMasterReplicaConnection<K, V>> connectAsync(
            RedisClient redisClient,
            RedisCodec<K, V> codec, List<RedisNodeDescription> redisURIs) {
        return transformAsyncConnectionException(
                connectAsyncSentinelOrStaticSetup(redisClient, codec, redisURIs), redisURIs);
    }

    private static <T> T getConnection(CompletableFuture<T> connectionFuture, Object context) {

        try {
            return connectionFuture.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw RedisConnectionException.create(context.toString(), e);
        } catch (Exception e) {

            if (e instanceof ExecutionException) {

                // filter intermediate RedisConnectionException exceptions that bloat the stack trace
                if (e.getCause() instanceof RedisConnectionException
                    && e.getCause().getCause() instanceof RedisConnectionException) {
                    throw RedisConnectionException.create(context.toString(), e.getCause().getCause());
                }

                throw RedisConnectionException.create(context.toString(), e.getCause());
            }

            throw RedisConnectionException.create(context.toString(), e);
        }
    }

    private static <T> CompletableFuture<T> transformAsyncConnectionException(CompletionStage<T> future,
                                                                              Object context) {

        return ConnectionFuture.from(null, future.toCompletableFuture()).thenCompose((v, e) -> {

            if (e != null) {

                // filter intermediate RedisConnectionException exceptions that bloat the stack trace
                if (e.getCause() instanceof RedisConnectionException
                    && e.getCause().getCause() instanceof RedisConnectionException) {
                    return Futures.failed(RedisConnectionException.create(context.toString(), e.getCause()));
                }
                return Futures.failed(RedisConnectionException.create(context.toString(), e));
            }

            return CompletableFuture.completedFuture(v);
        }).toCompletableFuture();
    }

}
