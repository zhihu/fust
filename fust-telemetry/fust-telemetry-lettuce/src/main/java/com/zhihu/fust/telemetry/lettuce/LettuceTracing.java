package com.zhihu.fust.telemetry.lettuce;

import io.lettuce.core.tracing.TraceContextProvider;
import io.lettuce.core.tracing.TracerProvider;
import io.lettuce.core.tracing.Tracing;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class LettuceTracing implements Tracing {

    private LettuceTracer tracer;

    @Override
    public TracerProvider getTracerProvider() {
        if (tracer == null) {
            tracer = new LettuceTracer(LettuceTelemetry.getTelemetry().getTracer());
        }
        return () -> tracer;
    }

    @Override
    public TraceContextProvider initialTraceContextProvider() {
        return new LettuceTraceContextProvider();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean includeCommandArgsInSpanTags() {
        return true;
    }

    @Override
    public Endpoint createEndpoint(SocketAddress socketAddress) {
        return new LettuceEndpoint(socketAddress);
    }

    public static class LettuceEndpoint implements Endpoint {

        private static final String UNKNOWN = "redis_unknown_0";
        private static final String FMT = "redis_%s_%d";
        private final String host;
        private final int port;

        public LettuceEndpoint(SocketAddress socketAddress) {
            if (socketAddress instanceof InetSocketAddress) {
                InetSocketAddress inet = (InetSocketAddress) socketAddress;
                host = inet.getHostName();
                port = inet.getPort();
            } else {
                host = "localhost";
                port = 0;
            }
        }

        public String getServiceAddress() {
            return host + ':' + port;
        }

        public String getServiceName() {
            if (port == 0) {
                return UNKNOWN;
            }
            return String.format(FMT, host, port).replace('.', '_');
        }
    }
}
