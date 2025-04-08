package com.zhihu.fust.armeria.grpc.server;

import java.util.Optional;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhihu.fust.commons.lang.PropertyUtils;
import com.zhihu.fust.commons.lang.SpiServiceLoader;
import com.zhihu.fust.commons.lang.concurrent.NamingPatternThreadFactory;
import com.zhihu.fust.core.env.Env;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.common.logging.RequestLog;
import com.linecorp.armeria.internal.shaded.guava.util.concurrent.MoreExecutors;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.ServerListenerAdapter;
import com.linecorp.armeria.server.docs.DocService;
import com.linecorp.armeria.server.docs.DocServiceBuilder;
import com.linecorp.armeria.server.grpc.GrpcService;
import com.linecorp.armeria.server.grpc.GrpcServiceBuilder;
import com.linecorp.armeria.server.grpc.HttpJsonTranscodingOptions;

import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import io.grpc.protobuf.services.ProtoReflectionService;

/**
 * gRPC server builder
 */
public final class GrpcServerBuilder {
    private static final Logger log = LoggerFactory.getLogger(GrpcServerBuilder.class);
    /**
     * 配置文档路径的属性 KEY
     */
    private static final String ARMERIA_RPC_DOC_URL_PATH = "armeria.rpc.doc.url.path";
    private static final String ARMERIA_RPC_DOC_DISABLE = "armeria.rpc.doc.disable";

    /**
     * 文档路径
     */
    private static final String DEFAULT_DOCS_PATH = "_docs";

    private final ThreadPoolExecutor startStopExecutor = new ThreadPoolExecutor(
            1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingDeque<>(),
            NamingPatternThreadFactory.of("grpc-server-start-stop-%d"));

    private final ServerBuilder serverBuilder;
    private final boolean registerShutdownHook;
    private final GrpcServiceBuilder builder = GrpcService.builder();
    private int maxRequestMessageLength = -1;
    private ScheduledExecutorService blockingTaskExecutor;
    private boolean shutdownOnStop;
    private Integer numThreads;
    private boolean useBlockingTaskExecutor;
    private boolean enableHealthCheckService;
    private boolean enableHttpJsonTranscoding;
    public HttpJsonTranscodingOptions httpJsonTranscodingOptions;

    private boolean enableStagingDocService;
    private final DocServiceBuilder docServiceBuilder;
    @Nullable
    private Consumer<RequestLog> requestMonitor;
    private final GrpcServerInterceptorFactory serverInterceptorFactory;

    public GrpcServerBuilder(ServerBuilder serverBuilder, boolean registerShutdownHook,
                             GrpcServerInterceptorFactory serverInterceptorFactory) {
        // 默认使用 blockingTaskExecutor
        this.useBlockingTaskExecutor = true;
        // 默认开启 grpc 健康状态服务
        this.enableHealthCheckService = true;
        this.serverBuilder = serverBuilder;
        this.registerShutdownHook = registerShutdownHook;
        this.docServiceBuilder = DocService.builder();
        this.serverInterceptorFactory = serverInterceptorFactory;
    }

    /**
     * create builder
     *
     * @param serverBuilder        server builder
     * @param registerShutdownHook enable shutdown hook
     */
    public GrpcServerBuilder(ServerBuilder serverBuilder, boolean registerShutdownHook) {
        this(serverBuilder, registerShutdownHook,
             SpiServiceLoader.get(GrpcServerInterceptorFactory.class)
                             .orElse(new DefaultGrpcServerInterceptorFactory()));
    }

    /**
     * builder with port
     *
     * @param port server port
     */
    public static GrpcServerBuilder builder(int port) {
        return builder(port, true);
    }

    /**
     * create a server builder with a check health service
     *
     * @param port                 server port
     * @param registerShutdownHook enable shutdown hook
     */
    public static GrpcServerBuilder builder(int port, boolean registerShutdownHook) {
        ServerBuilder sb = Server.builder();
        sb.http(port);
        // add check health
        sb.service("/check_health",
                   (ctx, req) -> HttpResponse.of(HttpStatus.OK, MediaType.PLAIN_TEXT_UTF_8, "OK"));

        return new GrpcServerBuilder(sb, registerShutdownHook);
    }

    /**
     * default value is 10M
     *
     * @param maxInboundMessageSizeBytes max request length
     * @deprecated Use {@link #maxRequestMessageLength(int)} instead.
     */
    @Deprecated
    public GrpcServerBuilder maxInboundMessageSizeBytes(int maxInboundMessageSizeBytes) {
        return maxRequestMessageLength(maxInboundMessageSizeBytes);
    }

    /**
     * add check health service, you can check service by /check_health
     * default is enabled
     * @param enableHealthCheckService enable
     */
    public GrpcServerBuilder enableHealthCheckService(boolean enableHealthCheckService) {
        this.enableHealthCheckService = enableHealthCheckService;
        return this;
    }

    /**
     * invoke when response send completed or receive cancel from grpc Client( client timeout)
     *
     * @param requestMonitor request monitor
     */
    public void requestMonitor(@Nullable Consumer<RequestLog> requestMonitor) {
        this.requestMonitor = requestMonitor;
    }

    public GrpcServerBuilder maxRequestMessageLength(int maxRequestMessageLength) {
        this.maxRequestMessageLength = maxRequestMessageLength;
        return this;
    }

    public DocServiceBuilder getDocServiceBuilder() {
        return docServiceBuilder;
    }

    /**
     * Enable DocService in Staging Env
     * Development and QA env will auto enable DocService
     * @param enabled enabled
     */
    public GrpcServerBuilder enableStagingDocService(boolean enabled) {
        this.enableStagingDocService = enabled;
        return this;
    }

    /**
     *  enable gRPC REST API
     *  default options is HttpJsonTranscodingQueryParamMatchRule.ORIGINAL_FIELD
     *  @param enabled enabled
     */
    public GrpcServerBuilder enableHttpJsonTranscoding(boolean enabled) {
        this.enableHttpJsonTranscoding = enabled;
        return this;
    }

    /**
     * enable gRPC REST API
     * @param options options
     */
    public GrpcServerBuilder enableHttpJsonTranscoding(HttpJsonTranscodingOptions options) {
        this.enableHttpJsonTranscoding = true;
        this.httpJsonTranscodingOptions = options;
        return this;
    }

    /**
     * Armeria grpc origin builder
     * your set armeria grpc option by this builder
     */
    public GrpcServiceBuilder getGrpcServiceBuilder() {
        return this.builder;
    }

    /**
     * armeria server builder
     * 设置 armeria 的原生配置
     */
    public ServerBuilder getServerBuilder() {
        return this.serverBuilder;
    }

    /**
     * default is unlimited
     *
     * @param maxOutboundMessageSizeBytes max response length
     * @deprecated Use {@link #maxResponseMessageLength(int)} instead.
     */
    @Deprecated
    public GrpcServerBuilder maxOutboundMessageSizeBytes(int maxOutboundMessageSizeBytes) {
        builder.maxResponseMessageLength(maxOutboundMessageSizeBytes);
        return this;
    }

    /**
     * max response size, default is unlimited
     **/
    public GrpcServerBuilder maxResponseMessageLength(int maxResponseMessageLength) {
        builder.maxResponseMessageLength(maxResponseMessageLength);
        return this;
    }

    /**
     * add armeria server interceptors
     * @param interceptors interceptors
     */
    public GrpcServerBuilder intercept(ServerInterceptor... interceptors) {
        builder.intercept(interceptors);
        return this;
    }

    /**
     * Adds a gRPC {@link BindableService} to this {@link GrpcServiceBuilder}. Most gRPC service
     * implementations are {@link BindableService}s.
     */
    public GrpcServerBuilder addService(BindableService service) {
        builder.addService(service);
        return this;
    }

    /**
     * custom blockingTaskExecutor
     *
     * @see ServerBuilder#blockingTaskExecutor(ScheduledExecutorService, boolean)
     */
    public GrpcServerBuilder blockingTaskExecutor(ScheduledExecutorService blockingTaskExecutor,
                                                  boolean shutdownOnStop) {
        this.blockingTaskExecutor = blockingTaskExecutor;
        this.shutdownOnStop = shutdownOnStop;
        return this;
    }

    /**
     * default useBlockingTaskExecutor is true
     */
    public GrpcServerBuilder useBlockingTaskExecutor(boolean useBlockingTaskExecutor) {
        this.useBlockingTaskExecutor = useBlockingTaskExecutor;
        return this;
    }

    /**
     * blockingTaskExecutor's default thread count is 200
     *
     * @see ServerBuilder#blockingTaskExecutor(int)
     */
    public GrpcServerBuilder blockingTaskExecutor(int numThreads) {
        this.numThreads = numThreads;
        return this;
    }

    public Server build() {
        // add request log decorator
        serverBuilder.decorator(new ArmeriaGrpcServerRequestLogDecorator(requestMonitor));

        // https://github.com/grpc/grpc-java/blob/master/documentation/server-reflection-tutorial.md
        builder.addService(ProtoReflectionService.newInstance());
        // interceptor
        builder.intercept(serverInterceptorFactory.create());
        if (httpJsonTranscodingOptions != null) {
            builder.enableHttpJsonTranscoding(httpJsonTranscodingOptions);
        } else {
            builder.enableHttpJsonTranscoding(enableHttpJsonTranscoding);
        }

        // see https://github.com/line/armeria/issues/4134
        builder.useBlockingTaskExecutor(useBlockingTaskExecutor);
        // https://github.com/grpc/grpc/blob/master/doc/health-checking.md
        builder.enableHealthCheckService(enableHealthCheckService);
        // request
        if (maxRequestMessageLength > 0) {
            builder.maxRequestMessageLength(maxRequestMessageLength);
            serverBuilder.maxRequestLength(maxRequestMessageLength);
        }

        boolean enableDocService = Env.isDevelop() || Env.isTesting()
                                   || (Env.isStaging() && enableStagingDocService);
        boolean forceDisable = PropertyUtils.getBoolProperty(ARMERIA_RPC_DOC_DISABLE).orElse(false);
        if (enableDocService && !forceDisable) {
            String docPath = Optional.ofNullable(PropertyUtils.getProperty(ARMERIA_RPC_DOC_URL_PATH))
                                     .orElse(DEFAULT_DOCS_PATH);
            // need enableUnframedRequests
            builder.enableUnframedRequests(true);
            if (!docPath.startsWith("/")) {
                // must start with /
                docPath = String.format("/%s", docPath);
            }
            serverBuilder.serviceUnder(docPath, docServiceBuilder.build());
        }
        serverBuilder.service(builder.build());

        // set executor
        if (useBlockingTaskExecutor) {
            if (blockingTaskExecutor != null) {
                serverBuilder.blockingTaskExecutor(blockingTaskExecutor, shutdownOnStop);
            } else {
                Optional.ofNullable(numThreads)
                        .ifPresent(serverBuilder::blockingTaskExecutor);
            }
        }

        serverBuilder.startStopExecutor(startStopExecutor);
        Server server = serverBuilder.build();
        server.addListener(new ServerListenerAdapter() {
            @Override
            public void serverStopped(Server server) throws Exception {
                MoreExecutors.shutdownAndAwaitTermination(startStopExecutor, 1, TimeUnit.SECONDS);
            }
        });

        if (registerShutdownHook) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                server.stop().join();
                log.info("Server has been stopped.");
            }));
        }

        return server;
    }

}
