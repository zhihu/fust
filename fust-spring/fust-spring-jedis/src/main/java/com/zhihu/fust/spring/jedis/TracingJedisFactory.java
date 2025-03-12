package com.zhihu.fust.spring.jedis;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.springframework.aop.framework.ProxyFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisFactory;

public class TracingJedisFactory extends JedisFactory {

    private final HostAndPort hostAndPort;

    protected TracingJedisFactory(HostAndPort hostAndPort, JedisClientConfig clientConfig) {
        super(hostAndPort, clientConfig);
        this.hostAndPort = hostAndPort;
    }

    @Override
    public PooledObject<Jedis> makeObject() throws Exception {
        PooledObject<Jedis> pooledObject = super.makeObject();
        Jedis jedis = pooledObject.getObject();
        ProxyFactory pf = new ProxyFactory();
        pf.setTarget(jedis);
        pf.addAdvice(new TracingJedisInterceptor(hostAndPort));
        return new DefaultPooledObject<>((Jedis) pf.getProxy());
    }
}
