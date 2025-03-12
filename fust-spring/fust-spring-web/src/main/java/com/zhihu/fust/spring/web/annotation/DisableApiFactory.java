package com.zhihu.fust.spring.web.annotation;

import java.lang.annotation.*;

/**
 * 禁用 ApiFactory 的自动处理
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DisableApiFactory {
}
