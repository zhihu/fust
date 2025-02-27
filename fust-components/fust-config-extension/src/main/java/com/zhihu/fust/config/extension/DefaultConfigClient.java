package com.zhihu.fust.config.extension;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.zhihu.fust.commons.lang.SpiServiceLoader;
import com.zhihu.fust.core.config.IConfigFileChangeListener;
import com.zhihu.fust.core.config.ConfigFileFormatEnum;
import com.zhihu.fust.core.config.IConfigProperties;
import com.zhihu.fust.core.config.IConfigPropertiesChangeListener;
import com.zhihu.fust.core.config.IConfigService;

public class DefaultConfigClient implements ConfigClient {
    private static final Logger log = LoggerFactory.getLogger(DefaultConfigClient.class);
    private static final JsonFactory JSON_FACTORY = new JsonFactoryBuilder()
            .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
            .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
            .enable(JsonReadFeature.ALLOW_MISSING_VALUES)
            .build();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(JSON_FACTORY)
            .registerModules(new JavaTimeModule(),
                             new SimpleModule().addDeserializer(GrayConfig.class,
                                                                new GrayConfigDeserializer()));

    private static final Cache<String, Object> CACHE = CacheBuilder.newBuilder()
                                                                   .maximumSize(1000)
                                                                   .expireAfterWrite(60, TimeUnit.SECONDS)
                                                                   .build();

    private final Set<String> changeKeySet = new HashSet<>();
    private final String defaultNamespace;

    public static class Stats {
        private final LongAdder cacheReadCount = new LongAdder();
        private final LongAdder cacheHitCount = new LongAdder();
        private final LongAdder cacheAddCount = new LongAdder();
        private final LongAdder listenerAddCount = new LongAdder();

        public LongAdder getCacheReadCount() {
            return cacheReadCount;
        }

        public LongAdder getCacheHitCount() {
            return cacheHitCount;
        }

        public LongAdder getListenerAddCount() {
            return listenerAddCount;
        }
    }

    private final ReentrantLock lock = new ReentrantLock();
    private final Stats stats = new Stats();
    private boolean enableStats = false;
    private final IConfigService configService;

    public DefaultConfigClient() {
        this.configService = SpiServiceLoader.get(IConfigService.class)
                                             .orElseThrow(() -> new IllegalStateException(
                                                     "no implementation of IConfigService"));
        this.defaultNamespace = configService.getProvider().defaultNamespace();
    }

    public IConfigService getConfigService() {
        return configService;
    }

    private void postProcess(Object config) {
        if (config instanceof ConfigPostProcessor) {
            ConfigPostProcessor processor = (ConfigPostProcessor) config;
            processor.init();
        }
    }

    public void setEnableStats(boolean value) {
        enableStats = value;
    }

    public Stats getStats() {
        return stats;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> clazz, String namespace, String key) {
        T value = (T) getFromCache(namespace, key);
        if (value != null) {
            return value;
        }
        IConfigProperties config = configService.getConfig(namespace);
        String property = config.getProperty(key, "");
        try {
            if (property.isEmpty()) {
                return null;
            }
            value = OBJECT_MAPPER.readValue(property, clazz);
            Optional.ofNullable(value)
                    .ifPresent(this::postProcess);
            addCache(namespace, ConfigFileFormatEnum.Properties, key, value);
            return value;
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    String.format("object ns|%s key|%s msg|%s", namespace, key, e + ""), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> listOf(TypeReference<List<T>> typeReference, String namespace, String key) {
        List<T> value = (List<T>) getFromCache(namespace, key);
        if (value != null) {
            return value;
        }

        IConfigProperties config = configService.getConfig(namespace);
        String property = config.getProperty(key, "");
        try {
            if (property.isEmpty()) {
                return Collections.emptyList();
            }
            value = OBJECT_MAPPER.readValue(property, typeReference);
            if (!isCollectionEmpty(value)) {
                value.forEach(this::postProcess);
            }

            addCache(namespace, ConfigFileFormatEnum.Properties, key, value);
            return value;
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    String.format("list ns|%s key|%s msg|%s", namespace, key, e + ""), e);
        }
    }

    private boolean isCollectionEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    @Override
    public String getString(String namespace, String key) {
        return configService.getConfig(namespace).getProperty(key, null);
    }

    @Override
    public Integer getInteger(String namespace, String key) {
        return configService.getConfig(namespace).getIntProperty(key, null);
    }

    @Override
    public Long getLong(String namespace, String key) {
        return configService.getConfig(namespace).getLongProperty(key, null);
    }

    @Override
    public Double getDouble(String namespace, String key) {
        return configService.getConfig(namespace).getDoubleProperty(key, null);
    }

    /**
     * ~~~ default namespace
     */
    @Override
    public <T> T get(Class<T> clazz, String key) {
        return get(clazz, "application", key);
    }

    @Override
    public <T> List<T> listOf(TypeReference<List<T>> typeReference, String key) {
        return listOf(typeReference, defaultNamespace, key);
    }

    @Override
    public String getString(String key) {
        return getString(defaultNamespace, key);
    }

    @Override
    public Integer getInteger(String key) {
        return getInteger(defaultNamespace, key);
    }

    @Override
    public Long getLong(String key) {
        return getLong(defaultNamespace, key);
    }

    @Override
    public Double getDouble(String key) {
        return getDouble(defaultNamespace, key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getConfigByJsonNamespace(Class<T> clazz, String namespace) {
        T value = (T) getFromCache(namespace, ConfigFileFormatEnum.JSON, "");
        if (value != null) {
            return value;
        }
        try {
            String content = configService.getConfigFile(
                                                  ConfigFileFormatEnum.JSON.getConfigFileName(namespace))
                                          .getContent();
            value = OBJECT_MAPPER.readValue(content, clazz);
            addCache(namespace, ConfigFileFormatEnum.JSON, "", value);
            postProcess(value);
            return value;
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("object ns|%s msg|%s", namespace, e + ""), e);
        }
    }

    /**
     * ~~~ config object cache
     */
    private String getCacheKey(String namespace, ConfigFileFormatEnum fileFormat, String key) {
        return String.format("%s:%s:%s", namespace, fileFormat.getValue(), key);
    }

    private Object getFromCache(String namespace, String key) {
        return getFromCache(namespace, ConfigFileFormatEnum.Properties, key);
    }

    private Object getFromCache(String namespace, ConfigFileFormatEnum fileFormat, String key) {
        Object ret = CACHE.getIfPresent(getCacheKey(namespace, fileFormat, key));
        if (enableStats) {
            stats.cacheReadCount.increment();
            if (ret != null) {
                stats.cacheHitCount.increment();
            }
        }
        return ret;
    }

    private void addCache(String namespace, ConfigFileFormatEnum fileFormat, String propKey, Object config) {
        String changeKey = String.format("%s:%s", namespace, fileFormat.getValue());
        if (!changeKeySet.contains(changeKey)) {
            lock.lock();
            try {
                // add listener to eviction cache
                if (!changeKeySet.contains(changeKey)) {
                    changeKeySet.add(changeKey);
                    if (enableStats) {
                        stats.listenerAddCount.increment();
                    }
                    log.debug("add eviction listener key|{}", changeKey);
                    if (fileFormat == ConfigFileFormatEnum.Properties) {
                        IConfigPropertiesChangeListener configChangeListener = event ->
                                event.changedKeys().stream()
                                     .map(v -> getCacheKey(namespace, fileFormat, v))
                                     .forEach(CACHE::invalidate);
                        configService.getConfig(namespace).addChangeListener(configChangeListener);
                    } else {
                        IConfigFileChangeListener configFileChangeListener =
                                changeEvent -> CACHE.invalidate(getCacheKey(namespace, fileFormat, propKey));
                        configService.getConfigFile(namespace, fileFormat).addChangeListener(
                                configFileChangeListener);
                    }
                }
            } finally {
                lock.unlock();
            }
        }
        CACHE.put(getCacheKey(namespace, fileFormat, propKey), config);
    }

}
