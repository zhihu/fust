package com.zhihu.fust.boot.jdbc;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhihu.fust.commons.lang.StringUtils;
import com.zhihu.fust.core.env.Env;
import com.zhihu.fust.spring.jdbc.*;
import com.zhihu.fust.spring.jdbc.config.DataSourceProperties;
import com.zhihu.fust.spring.jdbc.config.DatabaseProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@ComponentScan
public class JdbcAutoConfiguration implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(JdbcAutoConfiguration.class);
    private static final JsonFactory JSON_FACTORY = new JsonFactoryBuilder()
            .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
            .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
            .build();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(JSON_FACTORY);

    public JdbcAutoConfiguration() {
    }

    @Autowired(required = false)
    private List<DatabaseProperties> dataSourceProperties;

    @Autowired(required = false)
    private List<DataSourceAdapter> adapters;

    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnMissingBean(DataSourceFileProvider.class)
    public DataSourceFileProvider defaultDataSourceFileProvider() {
        return () -> String.format("classpath:db-%s.json", Env.getName().toLowerCase());
    }

    @Bean
    public JdbcConnectionFactory defaultJdbcConnectionFactory(ConnectionStrategy connectionStrategy) {
        return new JdbcConnectionFactory(connectionStrategy);
    }

    @Bean
    @ConditionalOnMissingBean(DataSourceDiscover.class)
    public DataSourceDiscover defaultDataSourceDiscover() {
        return new DefaultDataSourceDiscover();
    }

    @Bean
    @ConditionalOnMissingBean(ConnectionStrategy.class)
    public ConnectionStrategy connectionStrategy(DataSourceFileProvider dataSourceFileProvider,
                                                 DataSourceDiscover dataSourceDiscover) throws IOException {

        // single embedded data source, return empty strategy directly in unit test
        if (isUnitTest()) {
            Map<String, DataSource> dataSourceMap = applicationContext.getBeansOfType(DataSource.class);
            for (DataSource dataSource : dataSourceMap.values()) {
                if (dataSource instanceof EmbeddedDatabase) {
                    return EmptyConnectionStrategy.INSTANCE;
                }
            }
        }

        // inject by adapters
        if (adapters != null) {
            return createByAdapters(adapters);
        }

        // inject by properties
        if (dataSourceProperties != null) {
            return createByProperties(dataSourceProperties);
        }

        // inject by file provider
        return createByProvider(dataSourceFileProvider, dataSourceDiscover);

    }

    private JdbcConnectionStrategy createByAdapters(List<DataSourceAdapter> adapters) {
        String defaultDbName = adapters.stream()
                .filter(DataSourceAdapter::isDefaultDb)
                .findFirst()
                .map(DataSourceAdapter::getName)
                .orElse("");
        if (defaultDbName.isEmpty()) {
            defaultDbName = adapters.stream()
                    .findFirst()
                    .map(DataSourceAdapter::getName)
                    .orElse("");
        }

        return new JdbcConnectionStrategy(defaultDbName, adapters);
    }

    private JdbcConnectionStrategy createByProperties(List<DatabaseProperties> properties) {
        List<DataSourceAdapter> sourceAdapters = parseDbProperties(properties);
        String name = getDefaultDbName(properties, sourceAdapters.size());
        return new JdbcConnectionStrategy(name, sourceAdapters);
    }

    private JdbcConnectionStrategy createByProvider(DataSourceFileProvider fileProvider,
                                                    DataSourceDiscover discover) throws IOException {
        List<DatabaseProperties> properties = createDbProperties(fileProvider, discover);
        List<DataSourceAdapter> sourceAdapters = parseDbProperties(properties);
        String name = getDefaultDbName(properties, sourceAdapters.size());
        return new JdbcConnectionStrategy(name, sourceAdapters);
    }

    private List<DataSourceAdapter> parseDbProperties(List<DatabaseProperties> properties) {
        return properties.stream()
                .map(DataSourceAdapter::new)
                .collect(Collectors.toList());
    }

    private static String getDefaultDbName(List<DatabaseProperties> properties, int dbCount) {
        // 默认使用第一个
        String defaultName = properties.stream()
                .findFirst()
                .map(DatabaseProperties::getName)
                .orElse("");

        // 多个数据源配置，必须指定一个默认数据库名
        if (dbCount > 1) {
            List<String> defaults = properties.stream()
                    .filter(DatabaseProperties::isDefaultDatabase)
                    .map(DatabaseProperties::getName)
                    .collect(Collectors.toList());
            if (defaults.size() > 1) {
                throw new IllegalStateException("find too many default database name:" + defaults);
            }
            if (defaults.isEmpty()) {
                throw new IllegalStateException(
                        "multi-database need one default database name, but find no one");
            }
            // set default name
            defaultName = defaults.get(0);
        }
        return defaultName;
    }

    public List<DatabaseProperties> createDbProperties(DataSourceFileProvider dataSourceFileProvider,
                                                       DataSourceDiscover discover) throws IOException {
        String dsFile = dataSourceFileProvider.getDataSourceFile();
        if (StringUtils.isEmpty(dsFile)) {
            return Collections.emptyList();
        }

        URL url = null;
        try {
            url = ResourceUtils.getURL(dsFile);
        } catch (FileNotFoundException e) {
            logger.warn("not find ds file|{}", dsFile);
        }

        if (url == null) {
            return Collections.emptyList();
        }

        List<DatabaseProperties> properties = OBJECT_MAPPER.readValue(url,
                new TypeReference<List<DatabaseProperties>>() {
                });

        // check data source need discover
        if (discover != null) {
            for (DatabaseProperties p : properties) {
                if (p.isAutoDiscover()) {
                    List<DataSourceProperties> discoverList = null;
                    String resourceName = p.getName();
                    if (StringUtils.isNotEmpty(resourceName)) {
                        discoverList = discover.discover(resourceName);
                    }

                    // normally, just use the p.name
                    if (CollectionUtils.isEmpty(discoverList)) {
                        discoverList = discover.discover(p.getName());
                    }

                    if (CollectionUtils.isEmpty(discoverList)) {
                        throw new IllegalArgumentException("ds name: " + p.getName() + " no data source find!");
                    }

                    p.setDataSourcePropertiesList(discoverList);
                }
            }
        }

        return properties;
    }

    private static boolean isUnitTest() {
        Exception e = new Exception();
        StackTraceElement[] traceList = e.getStackTrace();
        for (StackTraceElement traceElement : traceList) {
            if (traceElement.getClassName().contains("junit")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
