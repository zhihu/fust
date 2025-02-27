package com.zhihu.fust.config.apollo;

import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigFileChangeListener;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.model.ConfigFileChangeEvent;
import com.zhihu.fust.core.config.IConfigFile;
import com.zhihu.fust.core.config.IConfigFileChangeListener;

import java.util.HashMap;
import java.util.Map;

public class ApolloConfigFile implements IConfigFile {
    private final ConfigFile configFile;
    private final Map<IConfigFileChangeListener, ConfigFileChangeListener> listenerMap = new HashMap<>();

    public ApolloConfigFile(ConfigFile configFile) {
        this.configFile = configFile;
    }

    @Override
    public String getContent() {
        return configFile.getContent();
    }

    @Override
    public boolean hasContent() {
        return configFile.hasContent();
    }

    @Override
    public String getNamespace() {
        ConfigFileFormat format = configFile.getConfigFileFormat();
        String namespace = configFile.getNamespace();
        if (format == ConfigFileFormat.Properties) {
            return namespace;
        }
        if (namespace.endsWith("." + format.getValue())) {
            return namespace;
        }
        return String.format("%s.%s", namespace, format.getValue());
    }

    @Override
    public void addChangeListener(IConfigFileChangeListener listener) {
        ConfigFileChangeListener configFileChangeListener = new ConfigFileChangeListener() {
            @Override
            public void onChange(ConfigFileChangeEvent changeEvent) {
                listener.onChange(new ApolloConfigFileChangeEvent(changeEvent));
            }
        };
        listenerMap.put(listener, configFileChangeListener);
        configFile.addChangeListener(configFileChangeListener);
    }

    @Override
    public boolean removeChangeListener(IConfigFileChangeListener listener) {
        ConfigFileChangeListener rawListener = listenerMap.remove(listener);
        if (rawListener == null) {
            return false;
        }
        return configFile.removeChangeListener(rawListener);
    }
}
