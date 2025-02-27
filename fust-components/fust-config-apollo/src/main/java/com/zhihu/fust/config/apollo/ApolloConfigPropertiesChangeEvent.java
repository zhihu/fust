package com.zhihu.fust.config.apollo;

import java.util.Set;

import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.zhihu.fust.core.config.IConfigPropertiesChange;
import com.zhihu.fust.core.config.IConfigPropertiesChangeEvent;

public class ApolloConfigPropertiesChangeEvent implements IConfigPropertiesChangeEvent {
    private final ConfigChangeEvent event;

    public ApolloConfigPropertiesChangeEvent(ConfigChangeEvent event) {
        this.event = event;
    }

    @Override
    public IConfigPropertiesChange getChange(String key) {
        return new ApolloPropertyConfigPropertiesChange(event.getChange(key));
    }

    @Override
    public Set<String> changedKeys() {
        return event.changedKeys();
    }

    @Override
    public String getNamespace() {
        return event.getNamespace();
    }
}
