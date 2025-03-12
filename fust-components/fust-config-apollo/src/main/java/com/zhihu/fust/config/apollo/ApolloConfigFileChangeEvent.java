package com.zhihu.fust.config.apollo;

import com.ctrip.framework.apollo.model.ConfigFileChangeEvent;
import com.zhihu.fust.core.config.ConfigChangeTypeEnum;
import com.zhihu.fust.core.config.IConfigFileChangeEvent;

public class ApolloConfigFileChangeEvent implements IConfigFileChangeEvent {
    private final ConfigFileChangeEvent event;

    public ApolloConfigFileChangeEvent(ConfigFileChangeEvent event) {
        this.event = event;
    }

    @Override
    public String getNamespace() {
        return event.getNamespace();
    }

    @Override
    public String getOldValue() {
        return event.getOldValue();
    }

    @Override
    public String getNewValue() {
        return event.getNewValue();
    }

    @Override
    public ConfigChangeTypeEnum getChangeType() {
        switch (event.getChangeType()) {
            case ADDED:
                return ConfigChangeTypeEnum.ADDED;
            case MODIFIED:
                return ConfigChangeTypeEnum.MODIFIED;
            case DELETED:
                return ConfigChangeTypeEnum.DELETED;
            default:
                throw new IllegalArgumentException("unknown change type");
        }
    }
}
