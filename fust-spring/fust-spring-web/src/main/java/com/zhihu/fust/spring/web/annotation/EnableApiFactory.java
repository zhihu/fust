package com.zhihu.fust.spring.web.annotation;

import com.zhihu.fust.spring.web.api.ApiFactory;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface EnableApiFactory {
    Class<? extends ApiFactory> value();
}
