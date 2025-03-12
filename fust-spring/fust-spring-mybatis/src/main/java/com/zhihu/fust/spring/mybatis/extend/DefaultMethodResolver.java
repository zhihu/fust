package com.zhihu.fust.spring.mybatis.extend;

import java.lang.reflect.Method;

import org.apache.ibatis.builder.annotation.MethodResolver;

public class DefaultMethodResolver extends MethodResolver {
    private final DefaultMapperAnnotationBuilder annotationBuilder;
    private final Method method;

    public DefaultMethodResolver(DefaultMapperAnnotationBuilder annotationBuilder, Method method) {
        super(annotationBuilder, method);
        this.annotationBuilder = annotationBuilder;
        this.method = method;
    }

    public void resolve() {
        annotationBuilder.parseStatement(method);
    }

}
