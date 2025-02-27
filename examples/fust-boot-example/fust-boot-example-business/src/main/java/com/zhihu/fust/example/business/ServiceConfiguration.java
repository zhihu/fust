package com.zhihu.fust.example.business;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * service configuration
 */
@Configuration
@ComponentScan
@MapperScan(annotationClass = Mapper.class)
public class ServiceConfiguration {
}