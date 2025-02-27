package com.zhihu.fust.core.config;

import java.util.Set;

public interface IConfigPropertiesChangeEvent {
    IConfigPropertiesChange getChange(String key);

    Set<String> changedKeys();

    String getNamespace();
}
