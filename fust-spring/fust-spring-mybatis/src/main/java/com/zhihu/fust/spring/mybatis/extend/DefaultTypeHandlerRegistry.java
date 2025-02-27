package com.zhihu.fust.spring.mybatis.extend;

import com.zhihu.fust.spring.mybatis.annotations.ColumnTypeHandler;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author zckongbai
 * @since 2024/9/29
 */
public class DefaultTypeHandlerRegistry {

    public static void registerGinTypeHandlers(Class<?> mapperClass, Class<?> entityClass,
                                               Configuration configuration) {
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        Field[] declaredFields = entityClass.getDeclaredFields();
        for (Field field : declaredFields) {
            if (!Modifier.isStatic(field.getModifiers())) { // 忽略静态属性
                Class<?> type = field.getType();
                ColumnTypeHandler typeHandler = field.getAnnotation(ColumnTypeHandler.class);
                if (typeHandler != null) {
                    Class<? extends TypeHandler<?>> handlerClass =
                            (Class<? extends TypeHandler<?>>) typeHandler.value();
                    typeHandlerRegistry.register(type, handlerClass);
                }
            }
        }
    }
}