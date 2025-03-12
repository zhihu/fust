package com.zhihu.fust.boot.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

@SpringBootApplication
@MapperScan(annotationClass = Mapper.class)
@ComponentScan
@Configuration
public class MybatisTestBoot {

    @Bean
    @Profile("test")
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setName("sku") // 数据库名
                .setType(EmbeddedDatabaseType.H2) // 内存数据类型: h2 database
                .addScript("classpath:data.sql") // 加载的 SQL
                .build();
    }
}
