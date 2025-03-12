package com.zhihu.fust.core.env;

import com.zhihu.fust.commons.io.FileUtils;
import com.zhihu.fust.provider.EnvironmentProvider;

import java.io.File;

import static com.zhihu.fust.core.env.DefaultEnvType.*;

/**
 * a default implementation of env
 **/
final class DefaultEnv implements EnvironmentProvider {
    public static final String ENV_NAME = "ENV";
    public static final String APP_NAME = "APP_NAME";
    public static final String SERVICE_NAME = "SERVICE_NAME";
    private static final String SERVICE_INSTANCE_ID = "SERVICE_INSTANCE_ID";

    public static final String CONFIG_ACCESS_KEY = "CONFIG_ACCESS_KEY";
    public static final String CONFIG_SERVER = "CONFIG_SERVER";
    public static final String VERSION = "VERSION";
    static final String GENERATED_DIR_NAME = "generated-cache";

    private static String generatedDir;

    @Override
    public String getName() {
        String value = getEnvOrProp(ENV_NAME);
        if (isEmptyString(value)) {
            return DEV.name();
        }
        return value;
    }

    @Override
    public boolean isDevelop() {
        return DEV.isEqual(getName());
    }

    @Override
    public boolean isProduction() {
        return PRODUCTION.isEqual(getName());
    }

    @Override
    public String getAppName() {
        return getEnvOrProp(APP_NAME);
    }

    @Override
    public String getServiceName() {
        return getEnvOrProp(SERVICE_NAME);
    }

    @Override
    public String getServiceInstanceId() {
        return getEnvOrProp(SERVICE_INSTANCE_ID);
    }

    @Override
    public String getVersion() {
        return getEnvOrProp(VERSION);
    }

    @Override
    public String getConfigAccessKey() {
        return getEnvOrProp(CONFIG_ACCESS_KEY);
    }

    @Override
    public String getConfigServer() {
        return getEnvOrProp(CONFIG_SERVER);
    }

    @Override
    public boolean isIntegration() {
        return INTEGRATION.isEqual(getName());
    }

    @Override
    public boolean isTesting() {
        return TESTING.isEqual(getName());
    }

    @Override
    public boolean isStaging() {
        return STAGING.isEqual(getName());
    }

    private static boolean isEmptyString(String s) {
        return s == null || s.isEmpty();
    }

    @Override
    public String getGeneratedDir() {
        if (generatedDir == null) {
            File dir = new File(GENERATED_DIR_NAME);
            if (!dir.exists() && !dir.mkdir()) {
                dir = new File(FileUtils.getTempDirectoryPath() + File.pathSeparator + GENERATED_DIR_NAME);
                if (!dir.exists() && !dir.mkdir()) {
                    throw new IllegalStateException("create dir failed! path is " + dir.getAbsolutePath());
                }
            }
            generatedDir = dir.getAbsolutePath();
        }
        return generatedDir;
    }

    private static String getEnvOrProp(String name) {
        String value = System.getProperty(name);
        if (isEmptyString(value)) {
            value = System.getenv(name);
        }
        return value;
    }
}
