package com.zhihu.fust.loggging.log4j2;

import com.zhihu.fust.core.logging.LogLevel;
import com.zhihu.fust.core.logging.LoggerConfiguration;
import com.zhihu.fust.core.logging.spi.LoggingConfigManager;
import com.zhihu.fust.core.logging.spi.LoggingSystemInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Log4j2LoggingSystemTest {
    private static final Logger log = LoggerFactory.getLogger(Log4j2LoggingSystemTest.class);
    private static Log4j2LoggingSystem loggingSystem;

    @BeforeAll
    static void setUp() {
        loggingSystem = new Log4j2LoggingSystem();
        loggingSystem.initialize();
    }

    @Test
    void testInitialize() {
        LoggingConfigManager configManager = loggingSystem.getLoggingConfigManager();
        String cfgFilePath = configManager.getLogCfgFilePath();

        assertTrue(new File(cfgFilePath).exists());
        assertEquals(LogLevel.INFO, configManager.getRootLevel());
        LoggingSystemInfo systemInfo = loggingSystem.getLoggingSystemInfo();
        assertEquals("META-INF/default-fust-log4j2-template.xml", systemInfo.getTemplateFile());
        assertEquals("log4j.configurationFile", systemInfo.getConfigFileKey());
    }

    @Test
    void testSetLogLevel() {
        loggingSystem.setLogLevel("", LogLevel.WARN);
        LoggerConfiguration root = loggingSystem.getLoggerConfiguration("");
        assertEquals(LogLevel.WARN, root.getEffectiveLevel());

        loggingSystem.setLogLevel("test", LogLevel.ERROR);
        LoggerConfiguration test = loggingSystem.getLoggerConfiguration("test");
        assertEquals(LogLevel.ERROR, test.getEffectiveLevel());
    }
}