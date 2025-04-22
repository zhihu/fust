package org.example.simple.business.dao;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import org.example.simple.business.model.UserModel;

import com.zhihu.fust.spring.mybatis.TemplateDao;
import com.zhihu.fust.spring.mybatis.extend.CollectionDriver;

@Mapper
public interface UserDao extends TemplateDao<UserModel> {
    // 表名常量
    String TABLE_NAME = "simple_user";

    /**
     * 根据ID批量查询用户
     */
    @Lang(CollectionDriver.class)
    @Select("SELECT * FROM " + TABLE_NAME + " WHERE id IN @ids")
    @ResultMap("UserModel")
    List<UserModel> findByIds(@Param("ids") Collection<Long> ids);

    // 批量删除
    @Lang(CollectionDriver.class)
    @Delete("DELETE FROM " + TABLE_NAME + " WHERE id IN @ids")
    int batchDelete(@Param("ids") List<Long> ids);

    // 根据名称批量查询
    @Lang(CollectionDriver.class)
    @Select("SELECT * FROM " + TABLE_NAME + " WHERE name IN @names")
    @ResultMap("UserModel")
    List<UserModel> findByNames(@Param("names") List<String> names);

}