package com.zhihu.fust.spring.redis.lettuce.internal;

import io.lettuce.core.*;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.cluster.models.partitions.Partitions;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.internal.AsyncConnectionProvider;
import io.lettuce.core.models.role.RedisInstance;
import io.lettuce.core.models.role.RedisNodeDescription;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * copy from MasterSlaveConnectionProvider
 *
 * @author yanzhuzhu
 * @since 2018/11/23
 */
public class DefaultLettuceConnectionProvider<K, V> {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(
            DefaultLettuceConnectionProvider.class);
    private final boolean debugEnabled = logger.isDebugEnabled();

    private final RedisURI initialRedisUri;
    private final AsyncConnectionProvider<ConnectionKey, StatefulRedisConnection<K, V>, CompletionStage<StatefulRedisConnection<K, V>>>
            connectionProvider;

    private List<RedisNodeDescription> knownNodes = new ArrayList<>();

    private boolean autoFlushCommands = true;
    private final Object stateLock = new Object();
    private ReadFrom readFrom;

    DefaultLettuceConnectionProvider(RedisClient redisClient, RedisCodec<K, V> redisCodec,
                                     RedisURI initialRedisUri,
                                     Map<RedisURI, StatefulRedisConnection<K, V>> initialConnections) {

        this.initialRedisUri = initialRedisUri;

        Function<ConnectionKey, CompletionStage<StatefulRedisConnection<K, V>>> connectionFactory =
                new DefaultMasterSlaveNodeConnectionFactory(redisClient, redisCodec);

        this.connectionProvider = new AsyncConnectionProvider<>(connectionFactory);

        for (Map.Entry<RedisURI, StatefulRedisConnection<K, V>> entry : initialConnections.entrySet()) {
            connectionProvider.register(toConnectionKey(entry.getKey()), entry.getValue());
        }
    }

    /**
     * Retrieve a {@link StatefulRedisConnection} by the intent.
     * candidates using the {@link ReadFrom} setting.
     *
     * @param intent command intent
     * @return the connection.
     */
    public StatefulRedisConnection<K, V> getConnection(Intent intent) {

        if (debugEnabled) {
            logger.debug("getConnection(" + intent + ")");
        }

        try {
            return getConnectionAsync(intent).get();
        } catch (RedisException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RedisCommandInterruptedException(e);
        } catch (ExecutionException e) {
            throw new RedisException(e.getCause());
        } catch (RuntimeException e) {
            throw new RedisException(e);
        }
    }

    /**
     * Retrieve a {@link StatefulRedisConnection} by the intent.
     * candidates using the {@link ReadFrom} setting.
     *
     * @param intent command intent
     * @return the connection.
     * @throws RedisException if the host is not part of the cluster
     */
    public CompletableFuture<StatefulRedisConnection<K, V>> getConnectionAsync(Intent intent) {

        if (debugEnabled) {
            logger.debug("getConnectionAsync(" + intent + ")");
        }

        if (readFrom != null && intent == Intent.READ) {
            List<RedisNodeDescription> selection = readFrom.select(new ReadFrom.Nodes() {
                @Override
                public List<RedisNodeDescription> getNodes() {
                    return knownNodes;
                }

                @Override
                public Iterator<RedisNodeDescription> iterator() {
                    return knownNodes.iterator();
                }
            });

            if (selection.isEmpty()) {
                throw new RedisException(
                        String.format("Cannot determine a node to read (Known nodes: %s) with setting %s",
                                knownNodes, readFrom));
            }

            try {

                Flux<StatefulRedisConnection<K, V>> connections = Flux.empty();

                for (RedisNodeDescription node : selection) {
                    connections = connections.concatWith(Mono.fromFuture(getConnection(node)));
                }

                return connections.filter(StatefulConnection::isOpen).next().switchIfEmpty(connections.next())
                        .toFuture();
            } catch (RuntimeException e) {
                throw bubble(e);
            }
        }

        return getConnection(getMaster());
    }

    protected CompletableFuture<StatefulRedisConnection<K, V>> getConnection(
            RedisNodeDescription redisNodeDescription) {

        RedisURI uri = redisNodeDescription.getUri();

        return connectionProvider.getConnection(toConnectionKey(uri)).toCompletableFuture();
    }

    /**
     * @return number of connections.
     */
    protected long getConnectionCount() {
        return connectionProvider.getConnectionCount();
    }

    /**
     * Retrieve a set of PoolKey's for all pooled connections that are
     * within the pool but not within the {@link Partitions}.
     *
     * @return Set of {@link ConnectionKey}s
     */
    private Set<ConnectionKey> getStaleConnectionKeys() {

        Map<ConnectionKey, StatefulRedisConnection<K, V>> map = new ConcurrentHashMap<>();
        connectionProvider.forEach(map::put);

        Set<ConnectionKey> stale = new HashSet<>();

        for (ConnectionKey connectionKey : map.keySet()) {

            if (connectionKey.host != null && findNodeByHostAndPort(knownNodes, connectionKey.host,
                    connectionKey.port) != null) {
                continue;
            }
            stale.add(connectionKey);
        }
        return stale;
    }

    /**
     * Close stale connections.
     */
    public void closeStaleConnections() {

        logger.debug("closeStaleConnections() count before expiring: {}", getConnectionCount());

        Set<ConnectionKey> stale = getStaleConnectionKeys();

        for (ConnectionKey connectionKey : stale) {
            connectionProvider.close(connectionKey);
        }

        logger.debug("closeStaleConnections() count after expiring: {}", getConnectionCount());
    }

    /**
     * Reset the command state of all connections.
     *
     * @see StatefulRedisConnection#reset()
     */
    @Deprecated
    public void reset() {
        connectionProvider.forEach(StatefulRedisConnection::reset);
    }

    /**
     * Close all connections.
     */
    public void close() {
        closeAsync().join();
    }

    /**
     * Close all connections asynchronously.
     *
     * @since 5.1
     */
    @SuppressWarnings("unchecked")
    public CompletableFuture<Void> closeAsync() {
        return connectionProvider.close();
    }

    /**
     * Flush pending commands on all connections.
     *
     * @see StatefulConnection#flushCommands()
     */
    public void flushCommands() {
        connectionProvider.forEach(StatefulConnection::flushCommands);
    }

    /**
     * Disable or enable auto-flush behavior for all connections.
     *
     * @param autoFlush state of autoFlush.
     * @see StatefulConnection#setAutoFlushCommands(boolean)
     */
    public void setAutoFlushCommands(boolean autoFlush) {

        synchronized (stateLock) {
            this.autoFlushCommands = autoFlush;
            connectionProvider.forEach(connection -> connection.setAutoFlushCommands(autoFlush));
        }
    }

    /**
     * @param knownNodes
     */
    public void setKnownNodes(Collection<RedisNodeDescription> knownNodes) {
        synchronized (stateLock) {

            this.knownNodes.clear();
            this.knownNodes.addAll(knownNodes);

            closeStaleConnections();
        }
    }

    /**
     * @return the current read-from setting.
     */
    public ReadFrom getReadFrom() {
        synchronized (stateLock) {
            return readFrom;
        }
    }

    public void setReadFrom(ReadFrom readFrom) {
        synchronized (stateLock) {
            this.readFrom = readFrom;
        }
    }

    public RedisNodeDescription getMaster() {

        for (RedisNodeDescription knownNode : knownNodes) {
            if (knownNode.getRole() == RedisInstance.Role.MASTER) {
                return knownNode;
            }
        }

        throw new RedisException(String.format("Master is currently unknown: %s", knownNodes));
    }

    class DefaultMasterSlaveNodeConnectionFactory
            implements Function<ConnectionKey, CompletionStage<StatefulRedisConnection<K, V>>> {

        private final RedisClient redisClient;
        private final RedisCodec<K, V> redisCodec;

        DefaultMasterSlaveNodeConnectionFactory(RedisClient redisClient, RedisCodec<K, V> redisCodec) {
            this.redisClient = redisClient;
            this.redisCodec = redisCodec;
        }

        @Override
        public ConnectionFuture<StatefulRedisConnection<K, V>> apply(ConnectionKey key) {

            RedisURI.Builder builder = RedisURI.Builder.redis(key.host, key.port)
                    .withLibraryVersion("")
                    .withLibraryName("")
                    .withSsl(initialRedisUri.isSsl())
                    .withVerifyPeer(initialRedisUri.isVerifyPeer())
                    .withStartTls(initialRedisUri.isStartTls());

            if (initialRedisUri.getPassword() != null && initialRedisUri.getPassword().length != 0) {
                builder.withPassword(initialRedisUri.getPassword());
            }

            if (initialRedisUri.getClientName() != null) {
                builder.withClientName(initialRedisUri.getClientName());
            }
            builder.withDatabase(initialRedisUri.getDatabase());

            ConnectionFuture<StatefulRedisConnection<K, V>> connectionFuture = redisClient.connectAsync(
                    redisCodec, builder.build());

            connectionFuture.thenAccept(connection -> {
                synchronized (stateLock) {
                    connection.setAutoFlushCommands(autoFlushCommands);
                }
            });

            return connectionFuture;
        }
    }

    private static ConnectionKey toConnectionKey(RedisURI redisURI) {
        return new ConnectionKey(redisURI.getHost(), redisURI.getPort());
    }

    /**
     * Connection to identify a connection by host/port.
     */
    static class ConnectionKey {

        private final String host;
        private final int port;

        ConnectionKey(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ConnectionKey)) {
                return false;
            }
            ConnectionKey that = (ConnectionKey) o;

            if (port != that.port) {
                return false;
            }
            return !(host != null ? !host.equals(that.host) : that.host != null);

        }

        @Override
        public int hashCode() {
            int result = (host != null ? host.hashCode() : 0);
            result = 31 * result + port;
            return result;
        }
    }

    static RedisNodeDescription findNodeByHostAndPort(Collection<RedisNodeDescription> nodes, String host,
                                                      int port) {
        for (RedisNodeDescription node : nodes) {
            RedisURI nodeUri = node.getUri();
            if (nodeUri.getHost().equals(host) && nodeUri.getPort() == port) {
                return node;
            }
        }
        return null;
    }

    public static RuntimeException bubble(Throwable t) {

        if (t instanceof ExecutionException) {
            return bubble(t.getCause());
        }

        if (t instanceof TimeoutException) {
            return new RedisCommandTimeoutException(t);
        }

        if (t instanceof InterruptedException) {

            Thread.currentThread().interrupt();
            return new RedisCommandInterruptedException(t);
        }

        if (t instanceof RedisException) {
            return (RedisException) t;
        }

        return new RedisException(t);
    }
}
