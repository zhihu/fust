package org.example.simple.business.config;


import javax.sql.DataSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Profile("test")
@Configuration
@EnableCaching
@ExtendWith(SpringExtension.class)
public class TestBeanConfig {
    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setName("db1") // 数据库名
                .setType(EmbeddedDatabaseType.H2) // 内存数据类型: h2 database
                .addScript("classpath:schema-h2.sql") // 加载数据库结构脚本
                .addScript("classpath:data-h2.sql") // 加载初始数据脚本
                .build();
    }
} 