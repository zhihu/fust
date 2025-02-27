package com.zhihu.fust.core.config;

public interface IConfigPropertiesChange {

    String getNamespace();

    String getPropertyName();

    String getOldValue();

    String getNewValue();

    ConfigChangeTypeEnum getChangeType();
}
