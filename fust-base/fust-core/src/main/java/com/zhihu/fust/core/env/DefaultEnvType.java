package com.zhihu.fust.core.env;

import javax.annotation.Nullable;

/**
 * standard env type
 **/
public enum DefaultEnvType {
    /**
     * Development environment: local development environment
     */
    DEV,

    /**
     * Integration environment: environment for merge code to unified code base,
     * then for unit test, integration test in ci build
     */
    INTEGRATION,

    /**
     * Testing environment: testing environment for QA
     */
    TESTING,

    /**
     * The staging environment is an environment that attempts to match production
     * as closely as possible in terms of resources used,
     * including computational load, hardware, and architecture.
     */
    STAGING,

    /**
     * Production environment: online environment for users
     **/
    PRODUCTION;

    public boolean isEqual(@Nullable String type) {
        return name().equalsIgnoreCase(type);
    }
}
