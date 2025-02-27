package com.zhihu.fust.core.config;

import com.zhihu.fust.provider.ConfigCustomProvider;

public interface IConfigService {

    void initialize();

    /**
     * get config by application namespace
     * application is an application.properties file in config center
     */
    IConfigProperties getAppConfig();

    IConfigProperties getConfig(String namespace);

    IConfigFile getConfigFile(String namespace);

    IConfigFile getConfigFile(String namespace, ConfigFileFormatEnum format);

    ConfigCustomProvider getProvider();
}
