package com.zhihu.fust.spring.web;

import com.zhihu.fust.spring.web.api.ApiError;

public class ApiException extends RuntimeException implements ApiError {
    private final String code;
    private final String message;
    private final String name;

    public ApiException(String name, String code, String message) {
        super(message);
        this.message = message;
        this.code = code;
        this.name = name;
    }

    public ApiException(String name, String code, String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getName() {
        return name;
    }
}
