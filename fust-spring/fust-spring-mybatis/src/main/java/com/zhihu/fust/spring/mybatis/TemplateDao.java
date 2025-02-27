package com.zhihu.fust.spring.mybatis;

import java.io.Serializable;
import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface TemplateDao<T> {
    boolean create(T model);

    T find(Serializable id);

    boolean update(@Param(Constants.ENTITY) T model);

    boolean remove(Serializable id);

    /**
     * 批量插入，单条 insert 多 values
     * 如果数据中存在 null 值，不会进行过滤，需要让数据都有默认值且默认值需要与数据库保存一致，会产生下面的问题：
     * 无法支持：model 默认值为 null 但是默认值不为 null 但传入 null 导致数据不准确，
     * 比如 mysql ZERO 日期默认值(传入 null 会使用当前时间做为默认值，而不是 schema 中指定的默认值）
     * @param models
     * @return
     */
    boolean batchCreate(@Param(Constants.ENTITIES) List<T> models);

    /**
     * 部分更新 model 信息
     * 忽略 null 字段的更新，部分更新对应主键 id
     * 需求：
     *  1.model 所有字段为包装类型
     *  2.model 中 id 必须存在
     * 建议：
     *  mysql 列类型设置为 not null default
     *
     * sql demo :
     * update item_info
     *      set schema_id   = case item_id
     *                            when 2 then "222"
     *          end,
     *          schema_info = case item_id
     *                            when 2 then schema_info
     *              end
     *       where id = 2
     *
     * @param model
     * @return
     */
    boolean patch(T model);

    /**
     * 批量部分更新 model 信息
     * 一致性保障: 成功则都成功，失败则都失败
     * 忽略 null 字段的更新，部分更新对应主键 id
     * 需求：
     *  1.model 所有字段为包装类型
     *  2.model 中 id 必须存在
     * 建议：
     *  mysql 列类型设置为 not null default
     *
     * sql demo:
     *      update item_info
     *      set schema_id   = case id
     *                            when 2 then "222"
     *                            when 1 then schema_id
     *                            when 111 then schema_id
     *          end,
     *          schema_info = case id
     *                            when 1 then "111"
     *                            when 2 then schema_info
     *                            when 111 then schema_info
     *              end
     *      where id in (1, 2, 111);
     *
     * @param models model list
     * @return true or false，
     */
    boolean batchPatch(@Param(Constants.ENTITIES) List<T> models);

}
