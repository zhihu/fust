package com.zhihu.fust.provider;

public interface ConfigCustomProvider {
    String configFile();

    String envFormatFile();

    String propAppName();

    String propAccessKey();

    String propConfigServer();

    String propConfigCacheDir();

    String configCacheDirName();

    default String defaultNamespace() {
        return "application";
    }

    default String grayConfigFileName() {
        return "gray-config";
    }
}
