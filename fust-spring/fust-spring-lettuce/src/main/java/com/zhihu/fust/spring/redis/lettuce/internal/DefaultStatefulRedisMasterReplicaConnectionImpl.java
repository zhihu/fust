package com.zhihu.fust.spring.redis.lettuce.internal;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;

import io.lettuce.core.ReadFrom;
import io.lettuce.core.StatefulRedisConnectionImpl;
import io.lettuce.core.api.push.PushListener;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection;
import io.lettuce.core.protocol.PushHandler;

/**
 * @author yanzhuzhu
 * @since 2018/11/23
 */
public class DefaultStatefulRedisMasterReplicaConnectionImpl<K, V> extends StatefulRedisConnectionImpl<K, V>
        implements
        StatefulRedisMasterReplicaConnection<K, V> {

    /**
     * Initialize a new connection.
     *
     * @param writer  the channel writer
     * @param codec   Codec used to encode/decode keys and values.
     * @param timeout Maximum time to wait for a response.
     */
    DefaultStatefulRedisMasterReplicaConnectionImpl(DefaultMasterReplicaChannelWriter writer,
                                                    RedisCodec<K, V> codec,
                                                    Duration timeout) {
        super(writer, NoOpPushHandler.INSTANCE, codec, timeout);
    }

    @Override
    public void setReadFrom(ReadFrom readFrom) {
        getChannelWriter().setReadFrom(readFrom);
    }

    @Override
    public ReadFrom getReadFrom() {
        return getChannelWriter().getReadFrom();
    }

    @Override
    public DefaultMasterReplicaChannelWriter getChannelWriter() {
        return (DefaultMasterReplicaChannelWriter) super.getChannelWriter();
    }

    private enum NoOpPushHandler implements PushHandler {
        /**
         * no op
         */
        INSTANCE;

        @Override
        public void addListener(PushListener listener) {

        }

        @Override
        public void removeListener(PushListener listener) {

        }

        @Override
        public Collection<PushListener> getPushListeners() {
            return Collections.emptyList();
        }

    }
}
