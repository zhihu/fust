package com.zhihu.fust.starter.web;

import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public class DefaultRequestMappingHandlerMapping
        extends RequestMappingHandlerMapping {
    public DefaultRequestMappingHandlerMapping() {
        this.setCorsProcessor(new EnvCorsProcessor());
    }
}
