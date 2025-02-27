package com.zhihu.fust.spring.web;

import com.zhihu.fust.spring.web.api.ApiError;

public class RestErrorResponse {

    public RestErrorResponse(ErrorInfo error) {
        this.error = error;
    }

    private final ErrorInfo error;

    public ErrorInfo getError() {
        return error;
    }

    public static RestErrorResponse error(ApiError error) {
        return new RestErrorResponse(new ErrorInfo(error));
    }
}
