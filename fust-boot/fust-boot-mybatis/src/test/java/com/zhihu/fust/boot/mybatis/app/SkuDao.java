package com.zhihu.fust.boot.mybatis.app;

import com.zhihu.fust.spring.mybatis.TemplateDao;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SkuDao extends TemplateDao<SkuModel> {

    String RESULT_MAP = "SkuModel";

    @Select({"select * from", SkuModel.TABLE_NAME, "where name = #{name}"})
    @ResultMap(RESULT_MAP)
    SkuModel findByName(@Param("name") String name);

    @Select({"select count(*) from", SkuModel.TABLE_NAME})
    int count();

}
