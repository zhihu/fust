package com.zhihu.fust.config.apollo;

import com.ctrip.framework.apollo.model.ConfigChange;
import com.zhihu.fust.core.config.IConfigPropertiesChange;
import com.zhihu.fust.core.config.ConfigChangeTypeEnum;

public class ApolloPropertyConfigPropertiesChange implements IConfigPropertiesChange {
    private final ConfigChange configChange;

    public ApolloPropertyConfigPropertiesChange(ConfigChange configChange) {
        this.configChange = configChange;
    }

    @Override
    public String getNamespace() {
        return configChange.getNamespace();
    }

    @Override
    public String getPropertyName() {
        return configChange.getPropertyName();
    }

    @Override
    public String getOldValue() {
        return configChange.getOldValue();
    }

    @Override
    public String getNewValue() {
        return configChange.getNewValue();
    }

    @Override
    public ConfigChangeTypeEnum getChangeType() {
        switch (configChange.getChangeType()) {
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
