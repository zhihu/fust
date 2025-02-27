package com.zhihu.fust.spring.redis.lettuce.internal;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import io.lettuce.core.protocol.CommandType;
import io.lettuce.core.protocol.ProtocolKeyword;

/**
 * @author yanzhuzhu
 * @since 2018/11/23
 */
public class DefaultReadOnlyCommands {
    private static final Set<CommandType> READ_ONLY_COMMANDS = EnumSet.noneOf(CommandType.class);

    static {
        for (DefaultReadOnlyCommands.CommandName commandNames : DefaultReadOnlyCommands.CommandName.values()) {
            READ_ONLY_COMMANDS.add(CommandType.valueOf(commandNames.name()));
        }
    }

    /**
     * @param protocolKeyword must not be {@literal null}.
     * @return {@literal true} if {@link ProtocolKeyword} is a read-only command.
     */
    public static boolean isReadOnlyCommand(ProtocolKeyword protocolKeyword) {
        return READ_ONLY_COMMANDS.contains(protocolKeyword);
    }

    /**
     * @return an unmodifiable {@link Set} of {@link CommandType read-only} commands.
     */
    public static Set<CommandType> getReadOnlyCommands() {
        return Collections.unmodifiableSet(READ_ONLY_COMMANDS);
    }

    enum CommandName {
        ASKING, BITCOUNT, BITPOS, CLIENT, COMMAND, DUMP, ECHO, EVAL, EVALSHA, EXISTS, //
        GEODIST, GEOPOS, GEORADIUS, GEORADIUSBYMEMBER, GEOHASH, GET, GETBIT, //
        GETRANGE, HEXISTS, HGET, HGETALL, HKEYS, HLEN, HMGET, HSCAN, HSTRLEN, //
        HVALS, INFO, KEYS, LINDEX, LLEN, LRANGE, MGET, PFCOUNT, PTTL, //
        RANDOMKEY, READWRITE, SCAN, SCARD, SCRIPT, //
        SDIFF, SINTER, SISMEMBER, SMEMBERS, SRANDMEMBER, SSCAN, STRLEN, //
        SUNION, TIME, TTL, TYPE, ZCARD, ZCOUNT, ZLEXCOUNT, ZRANGE, //
        ZRANGEBYLEX, ZRANGEBYSCORE, ZRANK, ZREVRANGE, ZREVRANGEBYLEX, ZREVRANGEBYSCORE, ZREVRANK, ZSCAN, ZSCORE,
    }
}
