package com.zhihu.fust.spring.mybatis.operations;

public enum SqlOperation {

    /**
     * 单个创建
     */
    CREATE("create", "<script>insert into %s "
                     + "<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\" >%s</trim> "
                     + "<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\" >%s</trim>"
                     + "</script>"),
    /**
     * 批量插入，无法过滤自动过滤 null
     * 不适用：Model 中有 null 但数据不允许 null 情况。
     * 注意：mysql ZERO 日期默认值存在使用 null 会使用当前时间，而不是默认值
     */
    BATCH_CREATE("batchCreate", "<script>insert into %s(%s) values "
                                + "<foreach item='et' collection='list' open='' separator=',' close=''>(%s)</foreach>"
                                + "</script>"),

    BATCH_PATCH("batchPatch", "<script>"
                              + "update %s set "
                              + "<trim prefix=\"\" suffix=\"\" suffixOverrides=\",\" >%s</trim>"
                              + "where id in "
                              + "<foreach collection='list' item='et' separator=',' open='(' close=')'>"
                              + "#{et.id}"
                              + "</foreach>"
                              + "</script>"),

    /**
     * 仅支持 ID 全量更新
     */
    UPDATE("update", "<script>UPDATE %s SET %s WHERE %s</script>"),

    /**
     * 仅支持 ID 部分更新
     */
    PATCH("patch", "<script>UPDATE %s SET "
                   + "<trim prefix=\"\" suffix=\"\" suffixOverrides=\",\" >%s</trim>"
                   + " WHERE %s</script>"),

    /**
     * 仅支持 ID 查询
     */
    FIND("find", "SELECT * FROM %s WHERE `%s`=#{%s}"),

    /**
     * 仅支持 ID 删除
     */
    REMOVE("remove", "DELETE FROM %s WHERE %s");

    private String sql;
    private String method;

    SqlOperation(String method, String sql) {
        this.method = method;
        this.sql = sql;
    }

    String format(Object... args) {
        return String.format(sql, args);
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
