package com.zhihu.fust.config.apollo;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.zhihu.fust.core.config.IConfigFile;
import com.zhihu.fust.core.config.IConfigProperties;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

class ApolloConfigServiceTest {

    @Test
    void testApolloConfigInitialize() {
        ApolloConfigService configService = new ApolloConfigService();
        configService.initialize();
        assertEquals("SampleApp", System.getProperty(ApolloMetaConfigProperties.APOLLO_APP_ID_KEY));
        assertEquals("1234key", System.getProperty(ApolloMetaConfigProperties.APOLLO_ACCESS_KEY));
        assertEquals("/tmp/cache/apollo", System.getProperty(ApolloMetaConfigProperties.APOLLO_CACHE_DIR_KEY));
        assertEquals("localhost:8080", System.getProperty(ConfigConsts.APOLLO_META_KEY));
    }

    @Test
    void testConfigService() {
        ConfigServiceDelegate mock = Mockito.mock(ConfigServiceDelegate.class);
        ApolloConfigService configService = new ApolloConfigService();
        configService.setConfigServiceDelegate(mock);
        Mockito.when(mock.getConfig(anyString()))
                .thenAnswer((Answer<Config>) invocation -> {
                    String namespace = invocation.getArgument(0);
                    if ("application".equals(namespace)) {
                        Config applicationCfg = Mockito.mock(Config.class);
                        Mockito.when(applicationCfg.getProperty(anyString(), anyString()))
                                .thenAnswer((Answer<String>) invocationOnMock -> {
                                    String key = invocationOnMock.getArgument(0);
                                    if ("name".equals(key)) {
                                        return "fust";
                                    }
                                    if ("default-test".equalsIgnoreCase(key)) {
                                        return invocationOnMock.getArgument(1);
                                    }
                                    return "abc";
                                });
                        return applicationCfg;
                    }
                    if ("logging".equals(namespace)) {
                        Config loggingCfg = Mockito.mock(Config.class);
                        Mockito.when(loggingCfg.getProperty(anyString(), anyString()))
                                .thenAnswer((Answer<String>) invocationOnMock -> {
                                    String key = invocationOnMock.getArgument(0);
                                    if ("root.level".equals(key)) {
                                        return "INFO";
                                    }
                                    return "ERROR";
                                });
                        return loggingCfg;
                    }
                    return null;
                });

        Mockito.when(mock.getConfigFile(anyString(), any()))
                .thenAnswer((Answer<ConfigFile>) invocation -> {
                    String namespace = invocation.getArgument(0);
                    ConfigFileFormat format = invocation.getArgument(1);
                    if ("other".equals(namespace) && format == ConfigFileFormat.Properties) {
                        ConfigFile cfgFile = Mockito.mock(ConfigFile.class);
                        Mockito.when(cfgFile.getContent())
                                .thenAnswer((Answer<String>) invocationOnMock -> "other=abc");
                        Mockito.when(cfgFile.hasContent())
                                .thenReturn(true);
                        Mockito.when(cfgFile.getNamespace())
                                .thenReturn("other");
                        Mockito.when(cfgFile.getConfigFileFormat())
                                .thenReturn(ConfigFileFormat.Properties);
                        return cfgFile;
                    }
                    if ("other".equals(namespace) && format == ConfigFileFormat.YAML) {
                        ConfigFile cfgFile = Mockito.mock(ConfigFile.class);
                        Mockito.when(cfgFile.getContent())
                                .thenAnswer((Answer<String>) invocationOnMock -> "other: abc");
                        Mockito.when(cfgFile.hasContent())
                                .thenReturn(true);
                        Mockito.when(cfgFile.getNamespace())
                                .thenReturn("other");
                        Mockito.when(cfgFile.getConfigFileFormat())
                                .thenReturn(ConfigFileFormat.YAML);
                        return cfgFile;
                    }
                    return null;
                });

        IConfigProperties properties = configService.getAppConfig();
        assertEquals("fust", properties.getProperty("name", ""));
        assertEquals("1", properties.getProperty("default-test", "1"));
        assertEquals("abc", properties.getProperty("other", "1"));
        IConfigProperties loggingCfg = configService.getConfig("logging");
        assertEquals("INFO", loggingCfg.getProperty("root.level", ""));

        IConfigFile otherPropsFile = configService.getConfigFile("other.properties");
        assertEquals("other=abc", otherPropsFile.getContent());
        assertTrue(otherPropsFile.hasContent());
        assertEquals("other", otherPropsFile.getNamespace());

        IConfigFile otherYamlFile = configService.getConfigFile("other.yaml");
        assertEquals("other: abc", otherYamlFile.getContent());
        assertTrue(otherYamlFile.hasContent());
        assertEquals("other.yaml", otherYamlFile.getNamespace());

    }
}