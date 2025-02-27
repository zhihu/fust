package com.zhihu.fust.spring.mybatis.extend;

import com.zhihu.fust.commons.lang.StringUtils;
import com.zhihu.fust.spring.jdbc.DataSourceAdapter;
import com.zhihu.fust.spring.jdbc.DefaultDataSourceWrapper;
import com.zhihu.fust.spring.jdbc.JdbcConnectionFactory;
import com.zhihu.fust.spring.jdbc.JdbcConnectionStrategy;
import com.zhihu.fust.spring.mybatis.util.TableMetaUtil;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;

public class DefaultConfiguration extends Configuration {

    private final Logger log = LoggerFactory.getLogger(DefaultConfiguration.class);
    private final MapperRegistry registry = new DefaultMapperRegistry(this);
    private final Map<String, SqlSessionTemplate> sessionTemplateMap = new HashMap<>();
    private final Map<String, Configuration> configurationMap = new HashMap<>();
    private final String defaultName;

    public DefaultConfiguration(Map<String, DataSourceAdapter> targetDataSources,
                                DefaultExecutorInterceptorContext interceptorContext) {
        this(targetDataSources, interceptorContext, new ArrayList<>());
    }

    public DefaultConfiguration(Map<String, DataSourceAdapter> targetDataSources,
                                DefaultExecutorInterceptorContext interceptorContext,
                                List<Interceptor> interceptorList) {
        Collection<DataSourceAdapter> values = targetDataSources.values();
        defaultName = values.stream()
                .filter(DataSourceAdapter::isDefaultDb)
                .map(DataSourceAdapter::getName)
                .findFirst()
                .orElse("");

        // 生成多数据源的 SqlSessionTemplate
        targetDataSources.forEach((schemeName, adapter) -> {
            // put all adapters into the strategy, so we can get any adapter by schemeName
            JdbcConnectionStrategy strategy = new JdbcConnectionStrategy(schemeName, new ArrayList<>(values));
            JdbcConnectionFactory connectionFactory = new JdbcConnectionFactory(strategy);
            connectionFactory.setDirectMode(true);
            DefaultDataSourceWrapper sourceWrapper = new DefaultDataSourceWrapper(connectionFactory);
            SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
            factory.setDataSource(sourceWrapper);
            factory.setVfs(SpringBootVFS.class);
            Configuration configuration = new Configuration();
            factory.setConfiguration(configuration);

            if (CollectionUtils.isEmpty(interceptorList)) {
                Interceptor[] interceptors = {new DefaultExecutorInterceptor(interceptorContext)};
                factory.setPlugins(interceptors);
            } else {
                Interceptor[] interceptors = new Interceptor[interceptorList.size() + 1];
                interceptorList.toArray(interceptors);
                interceptors[interceptorList.size()] = new DefaultExecutorInterceptor(interceptorContext);
                factory.setPlugins(interceptors);
            }

            try {
                SqlSessionFactory sessionFactory = factory.getObject();
                Objects.requireNonNull(sessionFactory, "session factory is null");
                sessionTemplateMap.put(schemeName, new SqlSessionTemplate(sessionFactory));
                configurationMap.put(schemeName, configuration);
            } catch (Exception e) {
                log.error("DefaultConfiguration error", e);
                throw new IllegalArgumentException(e.getMessage());
            }
        });
    }

    @Override
    public MapperRegistry getMapperRegistry() {
        return registry;
    }

    @Override
    public void addMappers(String packageName, Class<?> superType) {
    }

    @Override
    public void addMappers(String packageName) {
        registry.addMappers(packageName);
    }

    @Override
    public <T> void addMapper(Class<T> type) {
        registry.addMapper(type);
    }

    @Override
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return registry.getMapper(type, sqlSession);
    }

    @Override
    public boolean hasMapper(Class<?> type) {
        return registry.hasMapper(type);
    }

    public SqlSession getSqlSession(Class<?> mapperClass, SqlSession sqlSession) {
        if (sessionTemplateMap.isEmpty()) {
            return sqlSession;
        }
        String schemeName = TableMetaUtil.getScheme(mapperClass);
        if (StringUtils.isEmpty(schemeName)) {
            schemeName = defaultName;
        }
        return sessionTemplateMap.get(schemeName);
    }

    public Configuration getSchemeConfig(String schemeName) {
        if (configurationMap.isEmpty()) {
            return this;
        }
        if (StringUtils.isEmpty(schemeName)) {
            schemeName = defaultName;
        }
        return configurationMap.get(schemeName);
    }
}
