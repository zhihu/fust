package com.zhihu.fust.spring.mybatis.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * db autofill column
 * when do update or insert, this column value will be ignored
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {FIELD})
public @interface DbAutoColumn {
}
