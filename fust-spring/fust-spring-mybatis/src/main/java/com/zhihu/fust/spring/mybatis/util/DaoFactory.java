package com.zhihu.fust.spring.mybatis.util;

import com.zhihu.fust.spring.mybatis.extend.DefaultConfiguration;
import com.zhihu.fust.spring.mybatis.extend.DefaultExecutorInterceptor;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

/**
 * Create Mapper manually
 **/
public final class DaoFactory {
    private final SqlSession sqlSession;
    private final DefaultConfiguration defaultConfiguration;

    public DaoFactory(DataSource dataSource) {
        defaultConfiguration = new DefaultConfiguration(Collections.emptyMap(), name -> {
        }, new ArrayList<>());
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setVfs(SpringBootVFS.class);
        factoryBean.setConfiguration(defaultConfiguration);
        Interceptor[] interceptors = {new DefaultExecutorInterceptor(null)};
        factoryBean.setPlugins(interceptors);
        try {
            sqlSession = Optional.ofNullable(factoryBean.getObject()).map(SqlSessionFactory::openSession)
                    .orElse(null);
            Objects.requireNonNull(sqlSession, "init sql session error");
        } catch (Exception e) {
            throw new IllegalStateException("get sql session error! msg|" + e.getMessage());
        }
    }

    public <T> T getMapper(Class<T> mapper) {
        if (!defaultConfiguration.hasMapper(mapper)) {
            defaultConfiguration.addMapper(mapper);
        }
        return sqlSession.getMapper(mapper);
    }
}
