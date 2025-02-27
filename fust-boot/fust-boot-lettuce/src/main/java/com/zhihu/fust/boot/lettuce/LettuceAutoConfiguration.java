package com.zhihu.fust.boot.lettuce;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zhihu.fust.spring.redis.lettuce.LettuceConfigFactory;
import com.zhihu.fust.spring.redis.common.DefaultRedisResourceDiscover;
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
public class LettuceAutoConfiguration {

    private static final JsonFactory JSON_FACTORY = new JsonFactoryBuilder()
            .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
            .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
            .build();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(JSON_FACTORY).registerModule(
            new JavaTimeModule());

    @Bean
    public List<DefaultRedisProperties> redisPropertiesList(RedisConfigFileProvider redisConfigFileProvider,
                                                            @Autowired(required = false)
                                                            RedisResourceDiscover resourceDiscover) {
        return new DefaultRedisPropertiesListReader()
                .getRedisPropertiesList(redisConfigFileProvider, resourceDiscover);

    }

    @Bean
    public RedisFactoryConfig lettuceRedisConfigFactory(
            List<DefaultRedisProperties> defaultRedisPropertiesList) {
        return new LettuceConfigFactory(defaultRedisPropertiesList);
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(RedisFactoryConfig configFactory) {
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
