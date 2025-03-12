package com.zhihu.fust.spring.mybatis;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static java.util.stream.Collectors.joining;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

import com.zhihu.fust.commons.lang.StringUtils;
import com.zhihu.fust.commons.lang.tuple.Pair;
import com.zhihu.fust.spring.mybatis.annotations.ManualId;
import com.zhihu.fust.spring.mybatis.annotations.DbAutoColumn;

public class TableMeta {

    private static final String TEST_COLUMN_FMT = "<if test='%s != null'>`%s`,</if>";
    private static final String TEST_VALUE_FMT = "<if test='%s != null'>#{%s},</if>";
    private static final String QUOTE_COLUMN_FMT = "`%s`";

    private static final Function<Field, String> GEN_IF_TEST_COLUMN = v -> String.format(TEST_COLUMN_FMT,
                                                                                         v.getName(),
                                                                                         TableMeta.getColumnName(
                                                                                                 v));
    private static final UnaryOperator<String> GEN_IF_TEST_VALUE = v -> String.format(TEST_VALUE_FMT, v, v);
    private static final UnaryOperator<String> GEN_BATCH_VALUE_FMT = v -> String.format("#{et.%s}", v);
    private String schemaName;
    private String tableName;

    /**
     * key column name
     */
    private String keyColumn;

    /**
     * key property name
     */
    private String keyProperty;

    /**
     * column names (column name)
     */
    private final String columnsForCreate;

    /**
     * value names ( java property )
     */
    private final String valuesForCreate;

    /**
     * value names ( java property )
     */
    private final String valuesForUpdate;
    /**
     * column names
     */
    private final String columnsForBatchCreate;
    /**
     * #{et.col1}, #{et.col2}
     */
    private final String valuesForBatchCreate;
    /**
     * value names for patch
     */
    private final String valuesForPatch;

    private final String valuesForBatchPatch;

    private boolean supportPatch;

    private static final Predicate<Field> IS_KEY = field -> field.isAnnotationPresent(Id.class)
                                                            || field.isAnnotationPresent(ManualId.class);

    private static final Predicate<Field> NOT_STATIC = field -> !Modifier.isStatic(field.getModifiers());
    private static final Predicate<Field> NOT_SKIP_COLUMN = field -> !field.isAnnotationPresent(
            DbAutoColumn.class);
    private static final Predicate<Field> IS_MANUAL_ID = field -> field.isAnnotationPresent(ManualId.class);
    private final boolean useManualId;

    public TableMeta(Field[] declaredFields) {
        this.useManualId = Stream.of(declaredFields).anyMatch(IS_KEY.and(IS_MANUAL_ID));

        Predicate<Field> forCreate = NOT_STATIC.and(NOT_SKIP_COLUMN).and(IS_KEY.negate()).or(
                IS_KEY.and(IS_MANUAL_ID));
        this.columnsForCreate = Stream.of(declaredFields)
                                      .filter(forCreate)
                                      .map(GEN_IF_TEST_COLUMN)
                                      .collect(joining(""));

        this.columnsForBatchCreate = Stream.of(declaredFields)
                                           .filter(forCreate)
                                           .map(TableMeta::getColumnName)
                                           .map(x -> String.format(QUOTE_COLUMN_FMT, x))
                                           .collect(joining(","));

        this.valuesForCreate = Stream.of(declaredFields)
                                     .filter(forCreate)
                                     .map(Field::getName)
                                     .map(GEN_IF_TEST_VALUE)
                                     .collect(joining(""));

        this.valuesForBatchCreate = Stream.of(declaredFields)
                                          .filter(forCreate)
                                          .map(Field::getName)
                                          .map(GEN_BATCH_VALUE_FMT)
                                          .collect(joining(","));

        Predicate<Field> forUpdate = NOT_STATIC.and(NOT_SKIP_COLUMN).and(IS_KEY.negate());
        this.valuesForUpdate = Stream.of(declaredFields)
                                     .filter(forUpdate)
                                     .map(f -> Pair.of(getColumnName(f), f.getName()))
                                     .map(Constants.GEN_ENTITY_EQ_SQL_PAIR)
                                     .collect(joining(","));

        this.valuesForPatch = Stream.of(declaredFields)
                                    .filter(forUpdate)
                                    .map(f -> Pair.of(getColumnName(f), f.getName()))
                                    .map(Constants.GEN_EQ_IF_TEST_SQL_PAIR)
                                    .collect(joining(""));

        this.valuesForBatchPatch = Stream.of(declaredFields)
                                         .filter(forUpdate)
                                         .map(f -> Pair.of(getColumnName(f), f.getName()))
                                         .map(Constants.BATCH_GEN_EQ_IF_TEST_SQL_PAIR)
                                         .collect(joining(""));
        supportPatch = Stream.of(declaredFields)
                             .filter(forUpdate)
                             .noneMatch(t -> t.getType().isPrimitive());
    }

    public boolean isSupportPatch() {
        return supportPatch;
    }

    public static String getColumnName(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            if (StringUtils.isNotEmpty(column.name())) {
                return column.name();
            }
        }
        return LOWER_CAMEL.to(LOWER_UNDERSCORE, field.getName());
    }

    public String getValuesForBatchCreate() {
        return valuesForBatchCreate;
    }

    public String getColumnsForCreate() {
        return columnsForCreate;
    }

    public String getColumnsForBatchCreate() {
        return columnsForBatchCreate;
    }

    public String getValuesForCreate() {
        return valuesForCreate;
    }

    public String getValuesForUpdate() {
        return valuesForUpdate;
    }

    public String getValuesForPatch() {
        return valuesForPatch;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public boolean isUseManualId() {
        return useManualId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getKeyColumn() {
        return keyColumn;
    }

    public void setKeyColumn(String keyColumn) {
        this.keyColumn = keyColumn;
    }

    public String getKeyProperty() {
        return keyProperty;
    }

    public void setKeyProperty(String keyProperty) {
        this.keyProperty = keyProperty;
    }

    public String getValuesForBatchPatch() {
        return valuesForBatchPatch;
    }

    public boolean validate() {
        return StringUtils.isNotEmpty(tableName) && StringUtils.isNotEmpty(keyProperty);
    }
}
