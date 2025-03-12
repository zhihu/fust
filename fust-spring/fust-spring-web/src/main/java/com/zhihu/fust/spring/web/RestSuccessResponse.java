package com.zhihu.fust.spring.web;

public class RestSuccessResponse {

    private final Object data; // 数据

    public RestSuccessResponse(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public static RestSuccessResponse success(Object data) {
        return new RestSuccessResponse(data);
    }

}
