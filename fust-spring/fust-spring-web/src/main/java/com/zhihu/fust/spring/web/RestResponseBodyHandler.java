package com.zhihu.fust.spring.web;

import com.zhihu.fust.spring.web.annotation.EnableApiFactory;
import com.zhihu.fust.spring.web.api.ApiFactory;

import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.Nullable;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.io.IOException;
import java.util.List;

public class RestResponseBodyHandler extends RequestResponseBodyMethodProcessor {

    private final ApiFactory defaultApiFactory;

    public RestResponseBodyHandler(List<HttpMessageConverter<?>> converters,
                                   @Nullable List<Object> requestResponseBodyAdvice) {
        super(converters, null, requestResponseBodyAdvice);
        defaultApiFactory = new DefaultApiFactory();
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest)
            throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {

        if (!ApiFactory.isDisabledUseApiFactory(returnType)) {
            // 没有禁用，且不是分页响应
            if (!ApiFactory.isPagingResponse(returnType)) {
                ApiFactory apiFactory = getApiFactory(returnType);
                returnValue = apiFactory.success(returnValue);
            }
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            attributes.setAttribute("returnValue", returnValue, RequestAttributes.SCOPE_REQUEST);
        }

        super.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
    }

    private ApiFactory getApiFactory(MethodParameter returnType) {
        if (AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), EnableApiFactory.class)) {
            return ApiFactoryUtils.getApiFactory(returnType.getContainingClass());
        }
        return defaultApiFactory;
    }
}
