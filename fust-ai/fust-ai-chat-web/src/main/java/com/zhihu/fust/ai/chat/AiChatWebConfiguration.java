package com.zhihu.fust.ai.chat;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@MapperScan(annotationClass = Mapper.class)
public class AiChatWebConfiguration {
}
