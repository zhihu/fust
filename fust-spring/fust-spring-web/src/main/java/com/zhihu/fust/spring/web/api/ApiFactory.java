package com.zhihu.fust.spring.web.api;

import com.zhihu.fust.spring.web.annotation.DisableApiFactory;

import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;

/**
 * @author yanzhuzhu
 * @since 09/08/2018
 */
public interface ApiFactory {
    Object success(Object data);

    Object error(ApiError data);

    /**
     * 主动使用 DisableApiFactory 或者返回类型为 PaginationResponse 的接口不使用 ApiFactory
     */
    static boolean isDisabledUseApiFactory(MethodParameter returnType) {
        return returnType.hasMethodAnnotation(DisableApiFactory.class)
                || AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(),
                DisableApiFactory.class);
    }

    static boolean isPagingResponse(MethodParameter returnType) {
        return IPaginationResponse.class.isAssignableFrom(returnType.getParameterType());
    }
}
