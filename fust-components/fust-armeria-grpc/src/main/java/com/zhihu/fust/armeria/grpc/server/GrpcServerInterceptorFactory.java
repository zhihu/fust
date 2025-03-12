package com.zhihu.fust.armeria.grpc.server;

import io.grpc.ServerInterceptor;

public interface GrpcServerInterceptorFactory {
    ServerInterceptor create();
}
