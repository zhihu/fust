package com.zhihu.fust.config.apollo;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.zhihu.fust.core.config.IConfigProperties;
import com.zhihu.fust.core.config.IConfigPropertiesChangeListener;

public class ApolloConfigProperties implements IConfigProperties {
    private final Config config;

    public ApolloConfigProperties(Config config) {
        this.config = config;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return config.getProperty(key, defaultValue);
    }

    @Override
    public Integer getIntProperty(String key, Integer defaultValue) {
        return config.getIntProperty(key, defaultValue);
    }

    @Override
    public Long getLongProperty(String key, Long defaultValue) {
        return config.getLongProperty(key, defaultValue);
    }

    @Override
    public Double getDoubleProperty(String key, Double defaultValue) {
        return config.getDoubleProperty(key, defaultValue);
    }

    @Override
    public void addChangeListener(IConfigPropertiesChangeListener listener) {
        config.addChangeListener(new ConfigChangeListener() {
            @Override
            public void onChange(ConfigChangeEvent changeEvent) {
                listener.onChange(new ApolloConfigPropertiesChangeEvent(changeEvent));
            }
        });
    }
}
