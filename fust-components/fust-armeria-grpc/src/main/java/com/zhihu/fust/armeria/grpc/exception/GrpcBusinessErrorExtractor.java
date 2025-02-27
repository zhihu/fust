package com.zhihu.fust.armeria.grpc.exception;

import com.zhihu.fust.commons.lang.SpiServiceLoader;
import io.grpc.Metadata;

public class GrpcBusinessErrorExtractor {
    private static GrpcBusinessErrorProvider provider;

    public static GrpcBusinessErrorProvider getProvider() {
        if (provider == null) {
            provider = SpiServiceLoader.get(GrpcBusinessErrorProvider.class).orElse(new DefaultGrpcBusinessErrorProvider());
        }
        return provider;
    }

    public static GrpcBusinessError fromException(Exception e) {
        return getProvider().fromException(e);
    }

    public static GrpcBusinessError fromTrailers(Metadata trailers) {
        return getProvider().fromTrailers(trailers);
    }
}
