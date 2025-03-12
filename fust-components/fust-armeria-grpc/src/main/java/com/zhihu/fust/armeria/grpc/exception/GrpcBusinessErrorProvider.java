package com.zhihu.fust.armeria.grpc.exception;

import io.grpc.Metadata;

public interface GrpcBusinessErrorProvider {
    GrpcBusinessError fromException(Exception e);

    GrpcBusinessError fromTrailers(Metadata trailers);
}
