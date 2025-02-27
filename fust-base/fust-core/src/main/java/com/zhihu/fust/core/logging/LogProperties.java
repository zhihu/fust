package com.zhihu.fust.core.logging;

import com.zhihu.fust.commons.io.resource.ResourceUtils;
import com.zhihu.fust.commons.lang.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class LogProperties {
    private static final String LOGGER_PREFIX = "logger.";
    private static final String LOG_PROP_FILE = "classpath:log.properties";
    private final Properties properties;

    public LogProperties() {
        properties = new Properties();
        load();
    }

    public Properties getProperties() {
        return properties;
    }

    public String get(String key) {
        Object value = properties.get(key);
        if (value != null) {
            return String.valueOf(value);
        }
        return null;
    }

    public Map<String, String> getLoggers() {
        List<String> loggerNames = properties.stringPropertyNames().stream()
                .filter(x -> x.startsWith(LOGGER_PREFIX))
                .collect(Collectors.toList());

        Map<String, String> loggerMap = new HashMap<>();
        loggerNames.forEach(name -> {
            String level = get(name);
            if (!StringUtils.isBlank(level)) {
                loggerMap.put(name.substring(LOGGER_PREFIX.length()), level);
            }
        });
        return loggerMap;
    }

    private void load() {
        try {
            URL url = ResourceUtils.getURL(LOG_PROP_FILE);
            try (InputStream stream = url.openStream()) {
                properties.load(stream);
            }
        } catch (IOException e) {
            // ignore
        }
    }
}
