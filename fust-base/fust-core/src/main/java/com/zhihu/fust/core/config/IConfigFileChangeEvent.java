package com.zhihu.fust.core.config;

public interface IConfigFileChangeEvent {

    String getNamespace();

    String getOldValue();

    String getNewValue();

    ConfigChangeTypeEnum getChangeType();
}
