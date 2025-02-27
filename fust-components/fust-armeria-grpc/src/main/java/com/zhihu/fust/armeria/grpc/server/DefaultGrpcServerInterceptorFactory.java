package com.zhihu.fust.armeria.grpc.server;

import io.grpc.ServerInterceptor;

public class DefaultGrpcServerInterceptorFactory implements GrpcServerInterceptorFactory {
    @Override
    public ServerInterceptor create() {
        return new DefaultGrpcServerInterceptor();
    }
}
