package com.zhihu.fust.spring.redis.common;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zhihu.fust.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * 多 redis 实例的配置读取
 */
public class DefaultRedisPropertiesListReader implements RedisPropertiesListReader {

    private static final JsonFactory JSON_FACTORY = new JsonFactoryBuilder()
            .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
            .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
            .build();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(JSON_FACTORY).registerModule(
            new JavaTimeModule());

    public List<DefaultRedisProperties> getRedisPropertiesList(RedisConfigFileProvider redisConfigFileProvider,
                                                               RedisResourceDiscover resourceDiscover) {
        String filename = redisConfigFileProvider.getRedisConfigFile();
        if (StringUtils.isEmpty(filename)) {
            return Collections.emptyList();
        }

        List<DefaultRedisProperties> propertiesList = readFromFile(filename);

        // fulfill properties by resource discover
        for (DefaultRedisProperties redisProperties : propertiesList) {
            boolean isAutoDiscover = CollectionUtils.isEmpty(redisProperties.getNodes());
            if (isAutoDiscover) {
                if (resourceDiscover != null) {
                    List<DefaultRedisNodeProperties> discover = resourceDiscover.discover(
                            redisProperties.getName());
                    redisProperties.setNodes(discover);
                } else {
                    throw new IllegalArgumentException(
                            "no lettuce resourceDiscover, but need discover to find lettuce:"
                                    + redisProperties.getName());
                }

            }

            redisProperties.checkPrimary();
        }

        return propertiesList;
    }

    private List<DefaultRedisProperties> readFromFile(String filename) {
        try {
            URL url = ResourceUtils.getURL(filename);
            return OBJECT_MAPPER.readValue(url,
                    new TypeReference<List<DefaultRedisProperties>>() {
                    });
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read redis config file from " + filename, e);
        }

    }
}
