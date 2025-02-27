package com.zhihu.fust.example.business.dao;

import com.zhihu.fust.example.business.model.UserModel;
import com.zhihu.fust.spring.mybatis.TemplateDao;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDao extends TemplateDao<UserModel> {
}