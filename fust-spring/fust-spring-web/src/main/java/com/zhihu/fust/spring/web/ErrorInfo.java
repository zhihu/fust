package com.zhihu.fust.spring.web;

import com.zhihu.fust.spring.web.api.ApiError;

import java.io.Serializable;

/**
 * 统一的错误响应数据
 *
 * @author yanzhuzhu
 * @since 11/04/2018
 */
public class ErrorInfo implements Serializable {
    private final String code;
    private final String name;
    private final String message;

    public ErrorInfo(ApiError error) {
        this.code = error.getCode();
        this.name = error.getName();
        this.message = error.getMessage();
    }

    public String getCode() {
        return code;
    }


    public String getName() {
        return name;
    }


    public String getMessage() {
        return message;
    }
}
