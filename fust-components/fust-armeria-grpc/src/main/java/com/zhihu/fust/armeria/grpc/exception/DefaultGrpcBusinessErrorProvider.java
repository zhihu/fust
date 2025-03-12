package com.zhihu.fust.armeria.grpc.exception;

import io.grpc.Metadata;

public class DefaultGrpcBusinessErrorProvider implements GrpcBusinessErrorProvider {
    @Override
    public GrpcBusinessError fromException(Exception e) {
        return GrpcErrorInfo.fromException(e).orElse(null);
    }

    @Override
    public GrpcBusinessError fromTrailers(Metadata trailers) {
        return GrpcErrorInfo.fromTrailers(trailers).orElse(null);
    }
}
