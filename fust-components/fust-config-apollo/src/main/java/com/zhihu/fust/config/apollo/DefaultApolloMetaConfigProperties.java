package com.zhihu.fust.config.apollo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import com.zhihu.fust.commons.io.resource.ResourceUtils;
import com.zhihu.fust.commons.lang.SpiServiceLoader;
import com.zhihu.fust.commons.lang.StringUtils;
import com.zhihu.fust.core.env.Env;
import com.zhihu.fust.provider.ConfigCustomProvider;

public class DefaultApolloMetaConfigProperties implements ApolloMetaConfigProperties {
    private final Properties properties;
    private final ConfigCustomProvider provider;

    public DefaultApolloMetaConfigProperties() {
        properties = new Properties();
        provider = SpiServiceLoader.get(ConfigCustomProvider.class)
                .orElse(new DefaultConfigCustomProvider());
        load();
    }

    private void load() {

        // load env properties first
        try {
            String filename = String.format(provider.envFormatFile(), Env.getName().toLowerCase());
            URL url = ResourceUtils.getURL(filename);
            try (InputStream stream = url.openStream()) {
                properties.load(stream);
                // if success, just return
                return;
            }
        } catch (IOException e) {
            // ignore
        }

        try {
            URL url = ResourceUtils.getURL(provider.configFile());
            try (InputStream stream = url.openStream()) {
                properties.load(stream);
            }
        } catch (IOException e) {
            // ignore
        }

    }

    @Override
    public String getAppId() {
        return getValue(provider.propAppName(), Env.getAppName());
    }

    @Override
    public String getConfigAccessKey() {
        return getValue(provider.propAccessKey(), Env.getConfigAccessKey());
    }

    @Override
    public String getMetaAddr() {
        // get from jvm prop
        return getValue(provider.propConfigServer(), Env.getConfigServer());
    }

    @Override
    public String getCacheDir() {
        String defaultCacheDir = new File(Env.getGeneratedDir(), provider.configCacheDirName())
                .getAbsolutePath();
        return getValue(provider.propConfigCacheDir(), defaultCacheDir);
    }

    @Override
    public ConfigCustomProvider getProvider() {
        return provider;
    }

    private String getValue(String key, String defaultValue) {
        // get from jvm prop
        String value = System.getProperty(key);
        if (StringUtils.isBlank(value)) {
            // get from properties file
            value = (String) properties.get(key);
        }

        if (StringUtils.isBlank(value)) {
            // use default value
            value = defaultValue;
        }
        return value;
    }
}
