package com.zhihu.fust.grpc.examples.server;

import com.linecorp.armeria.common.RequestHeaders;
import com.zhihu.fust.armeria.grpc.GrpcTelemetry;
import com.zhihu.fust.core.env.Env;
import com.zhihu.fust.core.logging.spi.ILoggingSystem;
import com.zhihu.fust.armeria.grpc.server.GrpcServerBuilder;
import com.zhihu.fust.telemetry.api.Telemetry;
import com.zhihu.fust.telemetry.sdk.TelemetryInitializer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class MainServer {
    private static final Logger log = LoggerFactory.getLogger(MainServer.class);

    public static void main(String[] args) {
        Env.init();
        TelemetryInitializer.init();
        ILoggingSystem loggingSystem = ILoggingSystem.get();
        loggingSystem.initialize();
        int port = 8010;
        GrpcServerBuilder builder = GrpcServerBuilder.builder(port);
        builder.requestMonitor(reqLog -> {
            long totalTimeMs = TimeUnit.NANOSECONDS.toMillis(reqLog.totalDurationNanos());
            RequestHeaders headers = reqLog.requestHeaders();
            log.warn("monitor|headers={} totalTimeMs={}ms", headers, totalTimeMs);
        });
        builder.addService(new HelloWorldImpl())
                .addService(new EchoServiceImpl())
                .build()
                .start()
                .join();
    }
}
