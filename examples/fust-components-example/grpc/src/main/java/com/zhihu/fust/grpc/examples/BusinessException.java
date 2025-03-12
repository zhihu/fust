package com.zhihu.fust.grpc.examples;


import com.zhihu.fust.armeria.grpc.exception.GrpcBusinessError;

public class BusinessException extends RuntimeException implements GrpcBusinessError {

    private final String code;
    private final String description;

    // BusinessErrorCode 见下面的错误码枚举类示例
    public BusinessException(String code, String desc) {
        this.code = code;
        this.description = desc;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getMessage() {
        return description;
    }

}
