package com.zhihu.fust.core.env;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.ServiceLoader;

import com.zhihu.fust.provider.EnvironmentProvider;

/**
 * Application Env Util
 **/
public final class Env {

    private static volatile boolean envInitialized;
    private static final Object LOCK_FOR_INIT = new Object();
    private static EnvironmentProvider provider;

    /**
     * initial env and all spi
     */
    public static void init() {
        ensureEnvInitialized();
        // initialize all spi
    }

    public static EnvironmentProvider getProvider() {
        ensureEnvInitialized();
        return provider;
    }

    public static String getName() {
        return get().getName();
    }

    public static boolean isDevelop() {
        return get().isDevelop();
    }

    public static boolean isIntegration() {
        return get().isIntegration();
    }

    public static boolean isTesting() {
        return get().isTesting();
    }

    public static boolean isStaging() {
        return get().isStaging();
    }

    public static boolean isProduction() {
        return get().isProduction();
    }

    public static String getAppName() {
        return get().getAppName();
    }

    public static String getServiceName() {
        return get().getServiceName();
    }
    public static String getServiceInstanceId() {
        return get().getServiceInstanceId();
    }
    public static String getVersion() {
        return get().getVersion();
    }

    public static String getConfigAccessKey() {
        return get().getConfigAccessKey();
    }

    public static String getConfigServer() {
        return get().getConfigServer();
    }

    public static String getGeneratedDir() {
        return get().getGeneratedDir();
    }

    private static EnvironmentProvider get() {
        ensureEnvInitialized();
        return provider;
    }

    private static void ensureEnvInitialized() {
        if (envInitialized) {
            return;
        }

        synchronized (LOCK_FOR_INIT) {
            if (envInitialized) {
                return;
            }
            provider = AccessController.doPrivileged((PrivilegedAction<EnvironmentProvider>) () -> {
                ServiceLoader<EnvironmentProvider> services = ServiceLoader.load(EnvironmentProvider.class);
                Iterator<EnvironmentProvider> iterator = services.iterator();
                try {
                    if (iterator.hasNext()) {
                        return iterator.next();
                    }
                } catch (Throwable t) {
                    // Do nothing
                }
                return null;
            });

            if (provider == null) {
                provider = new DefaultEnv();
            }

            envInitialized = true;
        }
    }

}
