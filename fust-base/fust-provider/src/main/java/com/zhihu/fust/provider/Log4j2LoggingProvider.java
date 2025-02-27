package com.zhihu.fust.provider;

import java.util.List;

public interface Log4j2LoggingProvider {

    String getFilename();

    String getTemplateFile();

    String getDefaultValue(String key);

    List<String> getConfigKeys();
}
