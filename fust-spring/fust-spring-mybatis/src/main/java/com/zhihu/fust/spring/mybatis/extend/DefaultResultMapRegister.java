package com.zhihu.fust.spring.mybatis.extend;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;

import com.zhihu.fust.commons.lang.StringUtils;
import com.zhihu.fust.spring.mybatis.TableMeta;

public class DefaultResultMapRegister {
    public static void registerResultMap(Class<?> mapperClass, Class<?> entityClass,
                                         Configuration configuration) {
        registerResultMap(mapperClass, entityClass, configuration, null);
    }

    public static void registerResultMap(Class<?> mapperClass, Class<?> entityClass,
                                         Configuration configuration, String resultId) {
        Field[] declaredFields = entityClass.getDeclaredFields();
        List<ResultMapping> resultMappings = new LinkedList<>();
        for (Field field : declaredFields) {
            if (!Modifier.isStatic(field.getModifiers())) { // 忽略静态属性
                String propertyName = field.getName();
                String rawColumnName = TableMeta.getColumnName(field);
                String column = LOWER_CAMEL.to(LOWER_UNDERSCORE, rawColumnName);
                Class<?> type = field.getType();
                ResultMapping mapping = new ResultMapping.Builder(configuration, propertyName, column,
                                                                  type).build();
                resultMappings.add(mapping);
            }
        }

        if (StringUtils.isEmpty(resultId)) {
            resultId = entityClass.getSimpleName();
        }

        String absoluteId = mapperClass.getName() + "." + resultId;
        org.apache.ibatis.mapping.ResultMap resultMap = new org.apache.ibatis.mapping.ResultMap.Builder(
                configuration,
                absoluteId, entityClass, resultMappings).build();
        if (!configuration.hasResultMap(absoluteId)) {
            configuration.getTypeAliasRegistry().registerAlias(resultId, entityClass);
            configuration.addResultMap(resultMap);
        }

    }
}
