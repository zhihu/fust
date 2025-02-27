package com.zhihu.fust.armeria.grpc.client;

import io.grpc.stub.AbstractStub;

/**
 * grpc client factory
 **/
public final class GrpcClientFactory {
    /**
     * default client options, with write timeout=1s and response timeout=3s
     */
    public static <T extends AbstractStub<T>> T create(String targetName, Class<T> type) {
        return GrpcClientBuilder.builder(type)
                                .targetName(targetName)
                                .build();
    }

    /**
     * default client options, with write timeout=1s and response timeout=3s
     */
    public static <T extends AbstractStub<T>> T create(String host, int port, Class<T> type) {
        return GrpcClientBuilder.builder(type)
                                .endpoint(host, port)
                                .build();
    }

    /**
     * default client options, with write timeout=1s and response timeout=3s
     *
     * @param hostPort can use host, host:port， http://host or http://host:port
     */
    public static <T extends AbstractStub<T>> T createByHostPort(String hostPort, Class<T> type) {
        return GrpcClientBuilder.builder(type)
                                .endpoint(hostPort)
                                .build();
    }

    public static <T extends AbstractStub<T>> T create(String targetName, Class<T> type,
                                                       Integer maxInboundMessageSize) {
        return GrpcClientBuilder.builder(type)
                                .targetName(targetName)
                                .maxInboundMessageSize(maxInboundMessageSize)
                                .build();
    }

    private GrpcClientFactory() {
    }
}
