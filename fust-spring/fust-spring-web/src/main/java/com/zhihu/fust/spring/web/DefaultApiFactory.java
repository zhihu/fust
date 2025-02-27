package com.zhihu.fust.spring.web;

import com.zhihu.fust.spring.web.api.ApiError;
import com.zhihu.fust.spring.web.api.ApiFactory;

public class DefaultApiFactory implements ApiFactory {
    @Override
    public Object success(Object data) {
        return RestSuccessResponse.success(data);
    }

    @Override
    public Object error(ApiError error) {
        return RestErrorResponse.error(error);
    }
}
