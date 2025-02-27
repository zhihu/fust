package com.zhihu.fust.spring.web;

import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.JsonViewRequestBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.JsonViewResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 替换默认的 {@link RequestResponseBodyMethodProcessor} 为 {@link RestResponseBodyHandler }
 *
 * @author yanzhuzhu
 * @since 11/04/2018
 */
public class RestRequestMappingHandlerAdapter extends RequestMappingHandlerAdapter {

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        // 替换 RequestResponseBodyMethodProcessor 为 RestResponseBodyHandler
        List<Object> requestResponseBodyAdvice = Arrays.asList(new JsonViewRequestBodyAdvice(),
                new JsonViewResponseBodyAdvice());
        HandlerMethodReturnValueHandler handler = new RestResponseBodyHandler(getMessageConverters(),
                requestResponseBodyAdvice);
        List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>(getReturnValueHandlers());
        for (int i = 0; i < handlers.size(); i++) {
            if (handlers.get(i) instanceof RequestResponseBodyMethodProcessor) {
                handlers.set(i, handler);
            }
        }
        // 重置
        setReturnValueHandlers(handlers);
    }

}
