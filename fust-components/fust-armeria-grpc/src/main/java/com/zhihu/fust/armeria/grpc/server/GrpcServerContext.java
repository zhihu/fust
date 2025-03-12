package com.zhihu.fust.armeria.grpc.server;

import java.util.Map;
import java.util.Optional;

import io.grpc.Metadata;

public final class GrpcServerContext {
    public static final String KEY_HEADERS = "headers";

    private GrpcServerContext() {
    }

    private static final ThreadLocal<Map<String, Object>> CUSTOM_CONTEXT = new ThreadLocal<>();

    public static Map<String, Object> get() {
        return CUSTOM_CONTEXT.get();
    }

    public static Metadata getHeaders() {
        return (Metadata) Optional.ofNullable(CUSTOM_CONTEXT.get())
                                  .map(x -> x.get(KEY_HEADERS))
                                  .orElse(null);
    }

    public static String getHeader(String header) {
        Metadata.Key<String> key = Metadata.Key.of(header, Metadata.ASCII_STRING_MARSHALLER);
        return Optional.ofNullable(getHeaders())
                       .map(x -> x.get(key))
                       .orElse(null);
    }

    public static void set(Map<String, Object> map) {
        CUSTOM_CONTEXT.set(map);
    }

    public static void remove() {
        CUSTOM_CONTEXT.remove();
    }

}
