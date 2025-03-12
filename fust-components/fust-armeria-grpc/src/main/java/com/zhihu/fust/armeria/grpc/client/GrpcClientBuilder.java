package com.zhihu.fust.armeria.grpc.client;

import com.linecorp.armeria.client.Endpoint;
import com.linecorp.armeria.client.grpc.GrpcClients;
import com.linecorp.armeria.common.Scheme;
import com.linecorp.armeria.common.SessionProtocol;
import com.linecorp.armeria.common.grpc.GrpcSerializationFormats;
import com.linecorp.armeria.common.logging.RequestLog;
import com.zhihu.fust.commons.lang.SpiServiceLoader;
import com.zhihu.fust.commons.lang.StringUtils;
import io.grpc.stub.AbstractStub;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * grpc client builder
 */
public final class GrpcClientBuilder<T extends AbstractStub<T>> {

    private static final String HTTP = "http://";
    private static final String SCHEME_MARK = "://";

    private static final Scheme DEFAULT_GRPC_HTTP_SCHEME = Scheme.of(GrpcSerializationFormats.PROTO, SessionProtocol.HTTP);
    private final Class<T> stubType;

    private final EndpointGroupBuilder endpointGroupBuilder;

    @Nullable
    private String targetName;
    @Nullable
    private Endpoint endpoint;
    @Nullable
    private Integer maxInboundMessageSize;
    private Scheme scheme;
    @Nullable
    private Consumer<RequestLog> requestMonitor;


    public static <T extends AbstractStub<T>> GrpcClientBuilder<T> builder(Class<T> stubType) {
        return new GrpcClientBuilder<>(stubType);
    }

    private GrpcClientBuilder(Class<T> stubType) {
        this.endpointGroupBuilder = SpiServiceLoader.get(EndpointGroupBuilder.class)
                .orElse(null);
        this.stubType = stubType;
        this.scheme = DEFAULT_GRPC_HTTP_SCHEME;
    }

    /**
     * service target name
     */
    public GrpcClientBuilder<T> targetName(String targetName) {
        this.targetName = targetName;
        return this;
    }

    public GrpcClientBuilder<T> scheme(Scheme scheme) {
        Objects.requireNonNull(scheme, "scheme should not null");
        this.scheme = scheme;
        return this;
    }

    public GrpcClientBuilder<T> requestMonitor(Consumer<RequestLog> requestMonitor) {
        this.requestMonitor = requestMonitor;
        return this;
    }

    public GrpcClientBuilder<T> endpoint(String hostPort) {
        Objects.requireNonNull(hostPort, "hostPort is null");
        if (!hostPort.contains(SCHEME_MARK)) {
            hostPort = HTTP + hostPort;
        }
        URI uri = URI.create(hostPort);
        if (uri.getPort() == -1) {
            endpoint = Endpoint.of(uri.getHost());
        } else {
            endpoint = Endpoint.of(uri.getHost(), uri.getPort());
        }
        return this;
    }

    public GrpcClientBuilder<T> endpoint(String host, int port) {
        endpoint = Endpoint.of(host, port);
        return this;
    }

    public GrpcClientBuilder<T> maxInboundMessageSize(Integer maxInboundMessageSize) {
        this.maxInboundMessageSize = maxInboundMessageSize;
        return this;
    }

    @SuppressWarnings("unchecked")
    public T build() {
        com.linecorp.armeria.client.grpc.GrpcClientBuilder armeriaBuilder;
        if (StringUtils.isNotEmpty(targetName)) {
            if (endpointGroupBuilder != null) {
                armeriaBuilder = GrpcClients.builder(scheme, endpointGroupBuilder.build(targetName));
            } else {
                throw new IllegalArgumentException("required endpointGroupBuilder for targetName");
            }
        } else if (endpoint != null) {
            armeriaBuilder = GrpcClients.builder(scheme, endpoint);
        } else {
            throw new IllegalArgumentException("targetName or endpoint should not be null");
        }

        if (maxInboundMessageSize != null) {
            armeriaBuilder
                    .maxRequestMessageLength(maxInboundMessageSize);
        }

        String serviceName = targetName;
        if (StringUtils.isEmpty(serviceName)) {
            serviceName = "local";
        }

        armeriaBuilder.decorator(new ArmeriaGrpcClientRequestLogDecorator(requestMonitor));

        T stub = armeriaBuilder.build(stubType);
        stub = stub.withInterceptors(new TelemetryClientInterceptor(serviceName));
        return stub;
    }
}
