package com.zhihu.fust.spring.web.api;

public interface ApiError {
    default String getMessage() {
        return "";
    }
    
    default String getName() {
        return "";
    }

    default String getCode() {
        return "";
    }
}
