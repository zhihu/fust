package com.zhihu.fust.spring.jedis;

import org.springframework.aop.framework.ProxyFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;

public class TracingProxyFactory {

    public static Jedis createProxy(Jedis jedis, HostAndPort hostAndPort) {
        ProxyFactory pf = new ProxyFactory();
        pf.addAdvice(new TracingJedisInterceptor(hostAndPort));
        pf.setTarget(jedis);
        return (Jedis) pf.getProxy();
    }
}
