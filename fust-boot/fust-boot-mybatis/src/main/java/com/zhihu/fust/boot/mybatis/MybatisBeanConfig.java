package com.zhihu.fust.boot.mybatis;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import com.zhihu.fust.spring.jdbc.ConnectionStrategy;
import com.zhihu.fust.spring.jdbc.DataSourceAdapter;
import com.zhihu.fust.spring.jdbc.JdbcConnectionStrategy;
import com.zhihu.fust.spring.mybatis.extend.DefaultConfiguration;
import com.zhihu.fust.spring.mybatis.extend.DefaultExecutorInterceptor;
import com.zhihu.fust.spring.mybatis.extend.DefaultExecutorInterceptorContext;

@Configuration
@ComponentScan
public class MybatisBeanConfig {

    @Autowired(required = false)
    private DatabaseIdProvider databaseIdProvider;

    @Autowired(required = false)
    private DefaultExecutorInterceptorContext interceptorContext;

    @Autowired(required = false)
    private ConnectionStrategy connectionStrategy;

    @Autowired(required = false)
    private List<Interceptor> interceptorList;

    private Map<String, DataSourceAdapter> getTargetDataSources(ConnectionStrategy connectionStrategy) {
        if (connectionStrategy instanceof JdbcConnectionStrategy) {
            return ((JdbcConnectionStrategy) connectionStrategy).getTargetDataSources();
        }
        return Collections.emptyMap();
    }

    @Bean
    @ConditionalOnMissingBean(DefaultConfiguration.class)
    public DefaultConfiguration defaultConfiguration() {
        return new DefaultConfiguration(getTargetDataSources(connectionStrategy), interceptorContext,
                interceptorList);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource, DefaultConfiguration defaultConfiguration)
            throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        // 注入 DefaultConfiguration
        factory.setConfiguration(defaultConfiguration);

        if (CollectionUtils.isEmpty(interceptorList)) {
            Interceptor[] interceptors = {new DefaultExecutorInterceptor(interceptorContext)};
            factory.setPlugins(interceptors);
        } else {
            Interceptor[] interceptors = new Interceptor[interceptorList.size() + 1];
            interceptorList.toArray(interceptors);
            interceptors[interceptorList.size()] = new DefaultExecutorInterceptor(interceptorContext);
            factory.setPlugins(interceptors);
        }

        if (this.databaseIdProvider != null) {
            factory.setDatabaseIdProvider(this.databaseIdProvider);
        }

        return factory.getObject();
    }
}
