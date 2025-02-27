package com.zhihu.fust.core.env;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class EnvTest {


    @Test
    void testEnvVariables() {

        assertTrue(Env.isDevelop());

        System.setProperty("ENV", "DEV");
        assertTrue(Env.isDevelop());

        System.setProperty("ENV", "INTEGRATION");
        assertTrue(Env.isIntegration());

        System.setProperty("ENV", "TESTING");
        assertTrue(Env.isTesting());

        System.setProperty("ENV", "STAGING");
        assertTrue(Env.isStaging());

        System.setProperty("ENV", "PRODUCTION");
        assertTrue(Env.isProduction());

        System.setProperty("APP_NAME", "my-app");
        assertEquals("my-app", Env.getAppName());

        System.setProperty("SERVICE_NAME", "my-service");
        assertEquals("my-service", Env.getServiceName());

        System.setProperty("SERVICE_INSTANCE_ID", "my-instance");
        assertEquals("my-instance", Env.getServiceInstanceId());

        System.setProperty("VERSION", "1.0.0");
        assertEquals("1.0.0", Env.getVersion());

        System.setProperty("CONFIG_ACCESS_KEY", "my-key");
        assertEquals("my-key", Env.getConfigAccessKey());

        System.setProperty("CONFIG_SERVER", "my-server");
        assertEquals("my-server", Env.getConfigServer());

        String generatedDir = Env.getGeneratedDir();
        assertNotNull(generatedDir);


    }
}