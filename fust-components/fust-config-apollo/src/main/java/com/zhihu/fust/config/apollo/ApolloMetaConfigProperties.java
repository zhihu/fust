package com.zhihu.fust.config.apollo;

import com.zhihu.fust.provider.ConfigCustomProvider;

public interface ApolloMetaConfigProperties {
    String APOLLO_ACCESS_KEY = "apollo.access-key.secret";
    String APOLLO_CACHE_DIR_KEY = "apollo.cache-dir";
    String APOLLO_APP_ID_KEY = "app.id";

    String getAppId();

    String getConfigAccessKey();

    String getMetaAddr();

    String getCacheDir();

    ConfigCustomProvider getProvider();
}
