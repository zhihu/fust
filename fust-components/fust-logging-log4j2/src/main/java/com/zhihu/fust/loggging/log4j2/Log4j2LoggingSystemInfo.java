package com.zhihu.fust.loggging.log4j2;

import com.zhihu.fust.commons.lang.SpiServiceLoader;
import com.zhihu.fust.core.logging.spi.LoggingSystemInfo;
import com.zhihu.fust.provider.Log4j2LoggingProvider;

import java.nio.file.Paths;
import java.util.List;

public class Log4j2LoggingSystemInfo implements LoggingSystemInfo {
    /**
     * template filename
     */
    private final String templateFile;
    /**
     * config filename for save
     */
    private final String filename;
    private final String configFileKey;
    private final Log4j2LoggingProvider log4j2Provider;

    public Log4j2LoggingSystemInfo() {
        log4j2Provider = SpiServiceLoader.get(
                Log4j2LoggingProvider.class).orElse(new DefaultLog4J2LoggingProvider());
        templateFile = log4j2Provider.getTemplateFile();
        filename = log4j2Provider.getFilename();
        configFileKey = "log4j.configurationFile";
    }

    @Override
    public String getTemplateFile() {
        return templateFile;
    }

    @Override
    public String getConfigFileKey() {
        return configFileKey;
    }

    @Override
    public String getFilePath(String configDir) {
        return Paths.get(configDir, filename).toString();
    }

    @Override
    public List<String> getCustomKeys() {
        return log4j2Provider.getConfigKeys();
    }

    @Override
    public String getDefaultValue(String key) {
        return log4j2Provider.getDefaultValue(key);
    }
}
