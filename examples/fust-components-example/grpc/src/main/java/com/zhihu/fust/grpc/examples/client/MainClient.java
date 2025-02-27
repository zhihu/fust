package com.zhihu.fust.grpc.examples.client;

import com.linecorp.armeria.common.logging.RequestLog;
import com.zhihu.fust.armeria.grpc.client.GrpcClientBuilder;
import com.zhihu.fust.armeria.grpc.exception.GrpcBusinessError;
import com.zhihu.fust.core.env.Env;
import com.zhihu.fust.core.logging.spi.ILoggingSystem;
import com.zhihu.fust.telemetry.api.TraceUtils;
import com.zhihu.fust.telemetry.sdk.TelemetryInitializer;
import examples.api.echo.EchoServiceGrpc;
import examples.api.echo.Request;
import examples.api.hello.HelloRequest;
import examples.api.hello.HelloServiceGrpc;
import io.opentelemetry.context.Context;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


public class MainClient {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(MainClient.class);

    public static void main(String[] args) {
        Env.init();
        TelemetryInitializer.init();
        ILoggingSystem loggingSystem = ILoggingSystem.get();
        loggingSystem.initialize();

        Consumer<RequestLog> requestMonitor = reqLog -> {
            long totalTimeMs = TimeUnit.NANOSECONDS.toMillis(reqLog.totalDurationNanos());
            log.info("monitor|headers={} totalTimeMs={}ms", reqLog.requestHeaders(), totalTimeMs);
        };
        HelloServiceGrpc.HelloServiceBlockingStub stub = GrpcClientBuilder
                .builder(HelloServiceGrpc.HelloServiceBlockingStub.class)
                .requestMonitor(requestMonitor)
                .endpoint("127.0.0.1:8010")
                .build();

        EchoServiceGrpc.EchoServiceBlockingStub echoStub = GrpcClientBuilder
                .builder(EchoServiceGrpc.EchoServiceBlockingStub.class)
                .requestMonitor(requestMonitor)
                .endpoint("localhost:8010")
                .build();

        Context context = TraceUtils.ensureTraceId();
        try (var scope = context.makeCurrent()) {
            var resp = stub.sayHello(HelloRequest.newBuilder().setName("1").setFullName("2").build());
            log.info("resp:{}", resp.toString());

            log.info("------------------------hello error-------------------");
            try {
                var respError = stub.sayHello(HelloRequest.newBuilder().setName("biz_error2").setFullName("2").build());
            } catch (Exception e) {
                var error = GrpcBusinessError.fromException(e);
                error.ifPresent(bizError -> {
                    log.info("error code|{}, desc|{}", bizError.getCode(), bizError.getDescription());
                });
            }

            log.info("------------------------echo-------------------");
            var echoResp = echoStub
                    .echo(Request.newBuilder().setMessage("echo msg").build());
            log.info("echoResp {}", echoResp.getMessage());
        }
    }
}
