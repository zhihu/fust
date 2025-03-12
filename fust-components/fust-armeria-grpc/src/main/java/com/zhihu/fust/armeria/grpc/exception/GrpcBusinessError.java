package com.zhihu.fust.armeria.grpc.exception;

import java.util.Optional;

/**
 * Common business exception interface
 */
public interface GrpcBusinessError {


    /**
     * Grpc business system exception code
     *
     * @return code, such as "A0001","99999" etc.
     */
    String getCode();

    /**
     * Grpc business system exception description
     *
     * @return message, such as "USER NOT FOUND","PAYMENT TIME OUT" etc.
     */
    String getDescription();

    static Optional<GrpcBusinessError> fromException(Exception e) {
        return Optional.ofNullable(GrpcBusinessErrorExtractor.fromException(e));
    }
}
