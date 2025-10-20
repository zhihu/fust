package com.zhihu.fust.ai.chat;

import com.zhihu.fust.ai.chat.service.DefaultUserProviderService;
import com.zhihu.fust.ai.chat.service.UserProviderService;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@MapperScan(annotationClass = Mapper.class)
public class AiChatWebConfiguration {

    @ConditionalOnMissingBean(UserProviderService.class)
    @Bean
    public UserProviderService userProviderService() {
        return new DefaultUserProviderService();
    }
}
