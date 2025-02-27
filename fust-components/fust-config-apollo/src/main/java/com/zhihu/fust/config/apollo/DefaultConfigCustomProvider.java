package com.zhihu.fust.config.apollo;

import com.zhihu.fust.provider.ConfigCustomProvider;

public class DefaultConfigCustomProvider implements ConfigCustomProvider {
    private static final String CONFIG_FILE = "classpath:config.properties";
    private static final String CONFIG_ENV_FMT_FILE = "classpath:config-%s.properties";

    // properties
    private static final String PROP_APP_NAME = "app.id";
    private static final String PROP_ACCESS_KEY = "config.access-key";
    private static final String PROP_CONFIG_SERVER = "config.server";
    private static final String PROP_CONFIG_CACHE_DIR = "config.cache-dir";

    private static final String CONFIG_CACHE_DIR_NAME = "config-cache";

    @Override
    public String configFile() {
        return CONFIG_FILE;
    }

    @Override
    public String envFormatFile() {
        return CONFIG_ENV_FMT_FILE;
    }

    @Override
    public String propAppName() {
        return PROP_APP_NAME;
    }

    @Override
    public String propAccessKey() {
        return PROP_ACCESS_KEY;
    }

    @Override
    public String propConfigServer() {
        return PROP_CONFIG_SERVER;
    }

    @Override
    public String propConfigCacheDir() {
        return PROP_CONFIG_CACHE_DIR;
    }

    public String configCacheDirName() {
        return CONFIG_CACHE_DIR_NAME;
    }
}
