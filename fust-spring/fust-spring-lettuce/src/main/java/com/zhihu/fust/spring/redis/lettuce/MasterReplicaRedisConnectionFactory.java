package com.zhihu.fust.spring.redis.lettuce;

import java.util.ArrayList;
import java.util.List;

import com.zhihu.fust.spring.redis.lettuce.internal.DefaultRedisMasterReplicaNode;
import com.zhihu.fust.spring.redis.lettuce.internal.StaticMasterReplicaConnectionProvider;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionProvider;

import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.RedisClient;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.models.role.RedisNodeDescription;

/**
 * @author yanzhuzhu
 * @since 2018/11/23
 */
public class MasterReplicaRedisConnectionFactory extends LettuceConnectionFactory {
    private final String name;
    private final List<RedisNodeDescription> nodes = new ArrayList<>();
    private volatile boolean initialized = false;

    @Override
    public void afterPropertiesSet() {
        if (initialized) {
            // 避免多次调用
            return;
        }
        initialized = true;
        super.afterPropertiesSet();
    }

    public MasterReplicaRedisConnectionFactory(String name, LettuceClientConfiguration clientConfiguration) {
        super(new RedisStandaloneConfiguration(), clientConfiguration);
        this.name = name;
    }

    public void addNode(DefaultRedisMasterReplicaNode node) {
        nodes.add(node);
    }

    @Override
    protected LettuceConnectionProvider doCreateConnectionProvider(AbstractRedisClient client,
                                                                   RedisCodec<?, ?> codec) {
        ReadFrom readFrom = getClientConfiguration().getReadFrom().orElse(null);
        return new StaticMasterReplicaConnectionProvider((RedisClient) client, codec, nodes, readFrom);
    }

    @Override
    protected AbstractRedisClient createClient() {
        RedisClient redisClient = getClientConfiguration().getClientResources()
                                                          .map(RedisClient::create)
                                                          .orElseGet(RedisClient::create);
        getClientConfiguration().getClientOptions().ifPresent(redisClient::setOptions);
        return redisClient;
    }

    public String getName() {
        return name;
    }
}
