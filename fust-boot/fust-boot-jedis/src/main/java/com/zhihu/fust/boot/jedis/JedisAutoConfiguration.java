package com.zhihu.fust.boot.jedis;

import com.zhihu.fust.spring.jedis.JedisConfigFactory;
import com.zhihu.fust.spring.redis.common.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.List;

@Configuration
@ComponentScan
public class JedisAutoConfiguration {

    @Bean
    public List<DefaultRedisProperties> redisPropertiesList(RedisConfigFileProvider redisConfigFileProvider,
                                                            @Autowired(required = false)
                                                            RedisResourceDiscover resourceDiscover) {

        return new DefaultRedisPropertiesListReader().
                getRedisPropertiesList(redisConfigFileProvider, resourceDiscover);
    }

    @Bean
    public JedisConfigFactory jedisRedisFactoryConfig(
            List<DefaultRedisProperties> redisPropertiesList) {
        return new JedisConfigFactory(redisPropertiesList);
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(JedisConfigFactory configFactory) {
        return configFactory.getDefault();
    }

    @Bean
    @ConditionalOnMissingBean(RedisConfigFileProvider.class)
    public RedisConfigFileProvider defaultRedisConfigFileProvider() {
        return new DefaultRedisConfigFileProvider();
    }

    @Bean
    @ConditionalOnMissingBean(RedisResourceDiscover.class)
    public RedisResourceDiscover defaultRedisResourceDiscover() {
        return new DefaultRedisResourceDiscover();
    }

}
