package org.example.simple.business.api;

import com.zhihu.fust.spring.web.RestExceptionHandlerExceptionResolver;
import com.zhihu.fust.spring.web.RestRequestMappingHandlerAdapter;
import com.zhihu.fust.starter.web.DefaultRequestMappingHandlerMapping;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class ApiWebMvcRegistrations implements WebMvcRegistrations {

    @Override
    public RequestMappingHandlerAdapter getRequestMappingHandlerAdapter() {
        return new RestRequestMappingHandlerAdapter();
    }

    @Override
    public ExceptionHandlerExceptionResolver getExceptionHandlerExceptionResolver() {
        return new RestExceptionHandlerExceptionResolver();
    }

    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new DefaultRequestMappingHandlerMapping();
    }
}
