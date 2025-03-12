package com.zhihu.fust.spring.mybatis.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhihu.fust.commons.lang.StringUtils;
import com.zhihu.fust.spring.mybatis.TableMeta;
import com.zhihu.fust.spring.mybatis.annotations.ManualId;

public class TableMetaUtil {
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static Map<String, TableMeta> ginTableMetaCache = new HashMap<>();
    private static Map<String, Class<?>> mapperModelClassCache = new HashMap<>();

    public static synchronized TableMeta getTableMetaByModelClass(Class<?> entityClass) {

        TableMeta tableMeta = ginTableMetaCache.get(entityClass.getName());
        if (tableMeta != null) {
            return tableMeta;
        }

        tableMeta = new TableMeta(entityClass.getDeclaredFields());
        Table table = entityClass.getAnnotation(Table.class);
        tableMeta.setTableName(table.name());
        tableMeta.setSchemaName(table.schema());

        Optional<Field> keyField = findKeyField(entityClass);
        if (!keyField.isPresent()) {
            keyField = findKeyField(entityClass.getSuperclass());
        }
        if (!keyField.isPresent()) {
            throw new IllegalStateException("missing primary key in entityClass|" + entityClass.getName());
        }
        String keyProperty = keyField.map(Field::getName).orElse(null);
        String keyColumn = keyField.map(TableMeta::getColumnName).orElse(null);
        tableMeta.setKeyProperty(keyProperty);
        tableMeta.setKeyColumn(keyColumn);
        ginTableMetaCache.put(entityClass.getName(), tableMeta);

        return tableMeta;
    }

    private static Optional<Field> findKeyField(Class<?> clazz) {
        if (clazz == null) {
            return Optional.empty();
        }
        return Stream.of(clazz.getDeclaredFields()).filter(
                f -> f.isAnnotationPresent(Id.class) || f.isAnnotationPresent(ManualId.class)).findFirst();
    }

    public static synchronized Class<?> getModelClassByMapper(Class<?> mapperClass) {
        if (mapperClass == null) {
            return null;
        }
        String name = mapperClass.getName();
        Class<?> modelClass = mapperModelClassCache.get(name);
        if (modelClass != null) {
            return modelClass;
        }
        modelClass = ClassUtils.extractModelClass(mapperClass);
        mapperModelClassCache.put(name, modelClass);
        return modelClass;
    }

    public static TableMeta getMetaByMapperName(String name) {
        Class<?> modelClass = mapperModelClassCache.get(name);
        if (modelClass == null) {
            return null;
        }
        return getTableMetaByModelClass(modelClass);
    }

    public static String getScheme(Class<?> mapperClass) {
        if (mapperClass.isAnnotationPresent(Table.class)) {
            Table table = mapperClass.getAnnotation(Table.class);
            if (StringUtils.isNotEmpty(table.schema())) {
                return table.schema();
            }
        }
        Class<?> modelClass = TableMetaUtil.getModelClassByMapper(mapperClass);
        if (modelClass != null) {
            TableMeta tableMeta = TableMetaUtil.getTableMetaByModelClass(modelClass);
            return tableMeta.getSchemaName();
        }
        return "";
    }
}
