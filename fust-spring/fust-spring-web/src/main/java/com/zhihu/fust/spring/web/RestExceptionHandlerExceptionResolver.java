package com.zhihu.fust.spring.web;

import com.zhihu.fust.spring.web.annotation.EnableApiFactory;
import com.zhihu.fust.spring.web.api.ApiError;
import com.zhihu.fust.spring.web.api.ApiFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 处理 GinException 异常
 *
 * @author yanzhuzhu
 * @since 12/04/2018
 */
public class RestExceptionHandlerExceptionResolver extends ExceptionHandlerExceptionResolver {

    public static final Comparator<MediaType> QUALITY_VALUE_COMPARATOR = (mediaType1, mediaType2) -> {
        double quality1 = mediaType1.getQualityValue();
        double quality2 = mediaType2.getQualityValue();
        int qualityComparison = Double.compare(quality2, quality1);
        if (qualityComparison != 0) {
            return qualityComparison;
        } else if (mediaType1.isWildcardType() && !mediaType2.isWildcardType()) {
            return 1;
        } else if (mediaType2.isWildcardType() && !mediaType1.isWildcardType()) {
            return -1;
        } else if (!mediaType1.getType().equals(mediaType2.getType())) {
            return 0;
        } else if (mediaType1.isWildcardSubtype() && !mediaType2.isWildcardSubtype()) {
            return 1;
        } else if (mediaType2.isWildcardSubtype() && !mediaType1.isWildcardSubtype()) {
            return -1;
        } else if (!mediaType1.getSubtype().equals(mediaType2.getSubtype())) {
            return 0;
        } else {
            int paramsSize1 = mediaType1.getParameters().size();
            int paramsSize2 = mediaType2.getParameters().size();
            return Integer.compare(paramsSize2, paramsSize1);
        }
    };

    private MappingJackson2HttpMessageConverter converter;
    private ApiFactory defaultApiFactory;

    protected ModelAndView doResolveHandlerMethodException(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, Exception exception) {
        if (exception instanceof ApiError) {
            try {
                Object returnValue;
                MethodParameter returnType = handlerMethod.getReturnType();
                // 统一错误响应，默认使用 DefaultApiFactory
                ApiFactory apiFactory = getApiFactory(returnType);
                returnValue = apiFactory.error((ApiError) exception);
                handleResponseError(returnValue, request, response);
                return new ModelAndView();
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        } else {
            logger.error(exception.getMessage(), exception);
        }

        return super.doResolveHandlerMethodException(request, response, handlerMethod, exception);
    }

    private ApiFactory getApiFactory(MethodParameter returnType) {
        if (AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), EnableApiFactory.class)) {
            return ApiFactoryUtils.getApiFactory(returnType.getContainingClass());
        }
        return defaultApiFactory;
    }

    private void handleResponseError(Object returnValue, HttpServletRequest request,
                                     HttpServletResponse response) throws IOException {
        HttpInputMessage inputMessage = new ServletServerHttpRequest(request);
        List<MediaType> acceptedMediaTypes = inputMessage.getHeaders().getAccept();
        if (acceptedMediaTypes.isEmpty()) {
            acceptedMediaTypes = Collections.singletonList(MediaType.ALL);
        }
        if (acceptedMediaTypes.size() > 1) {
            acceptedMediaTypes.sort(QUALITY_VALUE_COMPARATOR);
        }
        HttpOutputMessage outputMessage = new ServletServerHttpResponse(response);
        Class<?> returnValueType = returnValue.getClass();
        for (MediaType acceptedMediaType : acceptedMediaTypes) {
            if (converter.canWrite(returnValueType, acceptedMediaType)) {
                converter.write(returnValue, acceptedMediaType, outputMessage);
                return;
            }
        }

    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        converter = new MappingJackson2HttpMessageConverter();
        defaultApiFactory = new DefaultApiFactory();
    }
}
