package com.zhihu.fust.boot.mybatis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zhihu.fust.spring.jdbc.JdbcConnectionHint;
import com.zhihu.fust.spring.mybatis.extend.DefaultExecutorInterceptorContext;

@Configuration
@ConditionalOnClass(JdbcConnectionHint.class)
public class JdbcExecutorInterceptorContext {
    @Bean
    @ConditionalOnMissingBean(DefaultExecutorInterceptorContext.class)
    public DefaultExecutorInterceptorContext defaultGinExecutorInterceptorContext() {
        return JdbcConnectionHint::setDatabaseName;
    }
}
