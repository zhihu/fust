package com.zhihu.fust.config.apollo;

import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.zhihu.fust.commons.lang.SpiServiceLoader;
import com.zhihu.fust.core.config.ConfigFileFormatEnum;
import com.zhihu.fust.core.config.IConfigFile;
import com.zhihu.fust.core.config.IConfigProperties;
import com.zhihu.fust.core.config.IConfigService;
import com.zhihu.fust.provider.ConfigCustomProvider;

import static com.ctrip.framework.apollo.core.ConfigConsts.APOLLO_META_KEY;

public class ApolloConfigService implements IConfigService {
    private ApolloMetaConfigProperties apolloConfigProperties;

    private ConfigServiceDelegate delegate;

    @Override
    public void initialize() {
        apolloConfigProperties = SpiServiceLoader
                .get(ApolloMetaConfigProperties.class)
                .orElse(new DefaultApolloMetaConfigProperties());
        // set apollo config
        setPropertyIfNotNull(ApolloMetaConfigProperties.APOLLO_APP_ID_KEY, apolloConfigProperties.getAppId());
        setPropertyIfNotNull(APOLLO_META_KEY, apolloConfigProperties.getMetaAddr());
        setPropertyIfNotNull(ApolloMetaConfigProperties.APOLLO_CACHE_DIR_KEY, apolloConfigProperties.getCacheDir());
        setPropertyIfNotNull(ApolloMetaConfigProperties.APOLLO_ACCESS_KEY, apolloConfigProperties.getConfigAccessKey());
        delegate = new ConfigServiceDelegate();
    }

    void setConfigServiceDelegate(ConfigServiceDelegate delegate) {
        this.delegate = delegate;
    }

    private void setPropertyIfNotNull(String key, String value) {
        if (value != null) {
            System.setProperty(key, value);
        }
    }

    @Override
    public IConfigProperties getAppConfig() {
        return getConfig("application");
    }

    @Override
    public IConfigProperties getConfig(String namespace) {
        return new ApolloConfigProperties(delegate.getConfig(namespace));
    }

    @Override
    public IConfigFile getConfigFile(String namespace) {
        ConfigFileFormat format = determineFileFormat(namespace);
        String apolloNameSpace = trimNamespaceFormat(namespace, format);
        return new ApolloConfigFile(delegate.getConfigFile(apolloNameSpace, format));
    }

    private ConfigFileFormat determineFileFormat(String namespaceName) {
        String lowerCase = namespaceName.toLowerCase();
        for (ConfigFileFormat format : ConfigFileFormat.values()) {
            if (lowerCase.endsWith("." + format.getValue())) {
                return format;
            }
        }

        return ConfigFileFormat.Properties;
    }

    private String trimNamespaceFormat(String namespaceName, ConfigFileFormat format) {
        String extension = "." + format.getValue();
        if (!namespaceName.toLowerCase().endsWith(extension)) {
            return namespaceName;
        }

        return namespaceName.substring(0, namespaceName.length() - extension.length());
    }

    @Override
    public IConfigFile getConfigFile(String namespace, ConfigFileFormatEnum format) {
        ConfigFileFormat apolloFileFormat = ConfigFileFormat.fromString(format.getValue());
        return new ApolloConfigFile(delegate.getConfigFile(namespace, apolloFileFormat));
    }

    @Override
    public ConfigCustomProvider getProvider() {
        return apolloConfigProperties.getProvider();
    }
}
