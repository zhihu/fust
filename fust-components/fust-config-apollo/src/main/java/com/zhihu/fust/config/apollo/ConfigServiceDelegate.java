package com.zhihu.fust.config.apollo;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;

public class ConfigServiceDelegate {
    public ConfigFile getConfigFile(String namespace, ConfigFileFormat configFileFormat) {
        return ConfigService.getConfigFile(namespace, configFileFormat);
    }

    public Config getConfig(String namespace) {
        return ConfigService.getConfig(namespace);
    }
}
