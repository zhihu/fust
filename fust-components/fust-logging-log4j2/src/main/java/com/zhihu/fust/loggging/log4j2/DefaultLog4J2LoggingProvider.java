package com.zhihu.fust.loggging.log4j2;

import com.zhihu.fust.core.logging.LogConstants;
import com.zhihu.fust.provider.Log4j2LoggingProvider;

import java.util.Arrays;
import java.util.List;

/**
 * 本质上，需要一个日志模板的处理器，需要支持自定义的模板及值的配置
 * 目前是写死的一套机制，只能自定义少量的 key 及 value
 */
public class DefaultLog4J2LoggingProvider implements Log4j2LoggingProvider {
    private static final String DEFAULT_LOG4J2_PATTERN =
            "[%level{length=1} %d{yyyy-MM-dd HH:mm:ss.SSS} $${ctx:trace_id:-0} %C{1}:%L $${env:HOSTNAME:-myHost}:%T] %X{extra} %msg%n";
    private static final String TEMPLATE_FILE = "META-INF/default-fust-log4j2-template.xml";
    private static final String FILENAME = "default-log4j2-auto.xml";

    @Override
    public String getFilename() {
        return FILENAME;
    }

    @Override
    public String getTemplateFile() {
        return TEMPLATE_FILE;
    }

    @Override
    public String getDefaultValue(String key) {
        if (key.equalsIgnoreCase(LogConstants.PATTERN_KEY)) {
            return DEFAULT_LOG4J2_PATTERN;
        }
        return LogConstants.getDefaultValue(key);
    }

    @Override
    public List<String> getConfigKeys() {
        return Arrays.asList(LogConstants.PATTERN_KEY, LogConstants.MAX_HISTORY_KEY, LogConstants.FILE_MAX_SIZE_KEY, LogConstants.FILE_BASE_SIZE_KEY);
    }

}
