package com.zhihu.fust.spring.redis.lettuce.internal;

import java.util.Objects;

import io.lettuce.core.RedisURI;
import io.lettuce.core.models.role.RedisNodeDescription;

public class DefaultRedisMasterReplicaNode implements RedisNodeDescription {
    private final RedisURI redisURI;
    private final Role role;

    public DefaultRedisMasterReplicaNode(String host, int port, String password, Role role) {

        RedisURI.Builder builder = RedisURI.Builder
                .redis(host, port)
                .withLibraryName("")
                .withLibraryVersion("")
                .withPassword(password.toCharArray());
        this.redisURI = builder.build();
        this.role = role;
    }

    @Override
    public RedisURI getUri() {
        return redisURI;
    }

    @Override
    public Role getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DefaultRedisMasterReplicaNode)) {
            return false;
        }
        DefaultRedisMasterReplicaNode that = (DefaultRedisMasterReplicaNode) o;
        return Objects.equals(redisURI, that.redisURI) &&
               role == that.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(redisURI, role);
    }

    @Override
    public String toString() {
        return "DefaultRedisMasterReplicaNode{" +
               "redisURI=" + redisURI +
               ", role=" + role +
               '}';
    }
}
