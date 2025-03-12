package com.zhihu.fust.core.logging.simple;

import com.zhihu.fust.core.logging.spi.LoggingSystemInfo;

import java.util.Collections;
import java.util.List;

public class SimpleLoggingSystemInfo implements LoggingSystemInfo {
    public static final SimpleLoggingSystemInfo INSTANCE = new SimpleLoggingSystemInfo();

    @Override
    public String getTemplateFile() {
        return null;
    }

    @Override
    public String getConfigFileKey() {
        return null;
    }

    @Override
    public String getFilePath(String configDir) {
        return null;
    }

    @Override
    public List<String> getCustomKeys() {
        return Collections.emptyList();
    }

    @Override
    public String getDefaultValue(String key) {
        return "";
    }
}
