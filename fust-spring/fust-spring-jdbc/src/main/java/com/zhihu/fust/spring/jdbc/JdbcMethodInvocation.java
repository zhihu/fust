package com.zhihu.fust.spring.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcMethodInvocation {

    private static Logger logger = LoggerFactory.getLogger(JdbcMethodInvocation.class);

    private final Method method;
    private final Object[] arguments;

    public JdbcMethodInvocation(Method method, Object[] arguments) {
        this.method = method;
        this.arguments = arguments;
    }

    /**
     * Invoke JDBC method.
     *
     * @param target target object
     */
    public void invoke(final Object target) {
        try {
            method.invoke(target, arguments);
        } catch (final IllegalAccessException | InvocationTargetException ex) {
            logger.error(ex.getMessage(), ex);
            throw new IllegalStateException("Invoke jdbc method exception:" + ex.getMessage());
        }
    }
}
