package com.zhihu.fust.spring.mybatis.extend;

/**
 * interceptor context
 */
public interface DefaultExecutorInterceptorContext {
    void setDatabaseName(String name);
}
