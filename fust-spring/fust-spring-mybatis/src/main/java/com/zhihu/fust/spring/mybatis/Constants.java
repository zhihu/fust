package com.zhihu.fust.spring.mybatis;

import java.util.function.Function;

import com.zhihu.fust.commons.lang.tuple.Pair;

public final class Constants {
    /**
     * 一个点
     */
    public static final String SPOT = ".";
    /**
     * 实体类
     */
    public static final String ENTITY = "et";
    /**
     * 实体类 带后缀 ==> .
     */
    public static final String ENTITY_SPOT = ENTITY + SPOT;

    /**
     * column = ${et.value}
     */
    public static final String ENTITY_EQ_FMT = "`%s` = #{" + ENTITY_SPOT + "%s}";

    /**
     * column = #{value}
     */
    public static final String EQ_TEST_FMT = "<if test='%s != null'>`%s` = #{%s},</if>";

    /**
     * column =
     * <foreach collection='list' item='et' separator=' ' open='case id' close='end'>
     * <choose>
     * <when test='et.value != null'>
     * when #{et.id} then #{et.value}
     * </when>
     * <otherwise>
     * when #{et.id} then #{et.value}
     * </otherwise>
     * </choose>
     * </foreach>
     */
    public static final String BATCH_EQ_TEST_FMT = "`%s` = "
                                                   + "<foreach collection='list' item='et' separator=' ' open='case id' close='end,'>"
                                                   + "<choose>"
                                                   + "<when test='et.%s != null'>"
                                                   + " when #{et.id} then #{et.%s} "
                                                   + "</when>"
                                                   + "<otherwise>"
                                                   + " when #{et.id} then `%s` "
                                                   + "</otherwise>"
                                                   + "</choose>"
                                                   + "</foreach>";

    /**
     * column = #{value}
     */
    public static final String EQ_FMT = "`%s` = #{%s}";

    public static final Function<Pair<String, String>, String> GEN_ENTITY_EQ_SQL_PAIR =
            v -> String.format(ENTITY_EQ_FMT, v.getLeft(), v.getRight());
    public static final Function<Pair<String, String>, String> GEN_EQ_SQL_PAIR =
            v -> String.format(EQ_FMT, v.getLeft(), v.getRight());
    public static final Function<Pair<String, String>, String> GEN_EQ_IF_TEST_SQL_PAIR =
            v -> String.format(EQ_TEST_FMT, v.getRight(), v.getLeft(), v.getRight());
    public static final Function<Pair<String, String>, String> BATCH_GEN_EQ_IF_TEST_SQL_PAIR =
            v -> String.format(BATCH_EQ_TEST_FMT, v.getLeft(), v.getRight(), v.getRight(), v.getLeft());

    public static final String ENTITIES = "list";
}
