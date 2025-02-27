package com.zhihu.fust.spring.jedis;

import com.github.fppt.jedismock.RedisServer;
import com.zhihu.fust.spring.redis.common.DefaultRedisNodeProperties;
import com.zhihu.fust.spring.redis.common.DefaultRedisProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JedisConfigFactoryTest {

    static RedisServer server;
    static int port = 16780;

    @BeforeAll
    public static void beforeAll() throws IOException {
        // https://github.com/fppt/jedis-mock
        server = RedisServer.newRedisServer(port)
                .start();
    }

    @AfterAll
    public static void afterAll() throws IOException {
        server.stop();
    }

    @Test
    void createJedisPool() {
        JedisConfigFactory factory = getJedisConfigFactory();
        RedisConnectionFactory test = factory.get("test");
        test.getConnection();
        StringRedisTemplate template = new StringRedisTemplate(test);
        template.opsForValue().set("key", "1");
        template.opsForHash().put("hash1", "k1", "1");

        assertEquals("1", template.opsForValue().get("key"));
        assertEquals("1", template.opsForHash().get("hash1", "k1"));
    }

    private static JedisConfigFactory getJedisConfigFactory() {
        List<DefaultRedisProperties> props = new ArrayList<>();
        DefaultRedisProperties prop = new DefaultRedisProperties();
        prop.setDefaultConnection(true);
        prop.setName("test");
        List<DefaultRedisNodeProperties> nodes = new ArrayList<>();
        DefaultRedisNodeProperties node = new DefaultRedisNodeProperties();
        node.setName("test-01");
        node.setHost("localhost");
        node.setPort(port);
        nodes.add(node);
        prop.setNodes(nodes);
        props.add(prop);

        return new JedisConfigFactory(props);
    }
}