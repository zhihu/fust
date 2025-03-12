package com.zhihu.fust.provider;

/**
 * <a href="https://www.codecademy.com/article/environments">...</a>
 * software environments
 * <p>
 * - Development environment: local development environment
 * - Integration environment: environment for merge code to unified code base, then for unit test, integration test in ci build
 * - Testing environment: testing environment for QA
 * - Staging environment:The staging environment is an environment that attempts to match
 * production as closely as possible in terms of resources used, including computational
 * load, hardware, and architecture.
 * - Production environment: online environment for users
 * <p>
 * fust framework will have default behaviors for different environment
 **/
public interface EnvironmentProvider {
    /**
     * environment name
     */
    String getName();

    /**
     * Development environment: local development environment
     */
    boolean isDevelop();

    /**
     * Integration environment: environment for merge code to unified code base,
     * then for unit test, integration test in ci build
     */
    boolean isIntegration();

    /**
     * Testing environment: testing environment for QA
     **/
    boolean isTesting();

    /**
     * The staging environment is an environment that attempts to match production
     * as closely as possible in terms of resources used, including computational load, hardware, and architecture.
     */
    boolean isStaging();

    /**
     * Production environment: online environment for users
     **/
    boolean isProduction();

    /**
     * microservices application name
     * for example, product
     */
    String getAppName();

    /**
     * microservices service name
     * for example, product-grpc-service, product-http-service
     */
    String getServiceName();

    default String getVersion() {
        return "";
    }

    /**
     * config access key
     */
    String getConfigAccessKey();

    String getConfigServer();

    /**
     * dir for Generated files
     */
    String getGeneratedDir();

    default String getServiceInstanceId() {
        return "";
    }
}
