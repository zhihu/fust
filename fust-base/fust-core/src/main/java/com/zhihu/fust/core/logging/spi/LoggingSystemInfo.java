package com.zhihu.fust.core.logging.spi;

import java.util.List;

public interface LoggingSystemInfo {

    /**
     * log config template
     */
    String getTemplateFile();

    /**
     * log config file
     */
    String getConfigFileKey();


    String getFilePath(String configDir);

    List<String> getCustomKeys();

    String getDefaultValue(String key);
}
