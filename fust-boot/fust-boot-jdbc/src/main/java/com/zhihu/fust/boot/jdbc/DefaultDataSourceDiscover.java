package com.zhihu.fust.boot.jdbc;

import static java.util.stream.Collectors.toList;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.zhihu.fust.commons.io.resource.URLUser;
import com.zhihu.fust.commons.lang.SpiServiceLoader;
import com.zhihu.fust.commons.lang.StringUtils;
import com.zhihu.fust.core.config.IConfigService;
import com.zhihu.fust.spring.jdbc.DataSourceDiscover;
import com.zhihu.fust.spring.jdbc.config.DataSourceProperties;
import com.zhihu.fust.spring.jdbc.config.JdbcConstants;

/**
 * Author: yzz
 * Date: 2019-05-16
 */
class DefaultDataSourceDiscover implements DataSourceDiscover {
    private static final Logger log = LoggerFactory.getLogger(DefaultDataSourceDiscover.class);

    /**
     * 环境变量注入格式
     * RES_MYSQL_{db_name}_{db_type}_{index}
     * db_name 数据库名
     * db_type 数据库类型（master/replica)
     * index 从 0 开始，一般从库可能有多个
     */
    private static final String MYSQL_FMT = "RES_MYSQL_%s_%s_%d";

    private static DataSourceProperties toDataSourceProperties(String name, URL url, boolean isMaster) {
        DataSourceProperties properties = new DataSourceProperties();
        Optional<URLUser> user = URLUser.of(url);
        String databaseName = url.getPath().substring(1);
        String jdbcUrl = String.format(JdbcConstants.JDBC_URL_FMT, url.getHost(), url.getPort(), databaseName);

        properties.setType(isMaster ? JdbcConstants.MASTER : JdbcConstants.REPLICA);
        properties.setName(name + "_" + properties.getType());
        properties.setUrl(jdbcUrl);

        user.ifPresent(u -> {
            properties.setUsername(u.getUsername());
            properties.setPassword(u.getPassword());
        });
        return properties;
    }

    @Override
    public List<DataSourceProperties> discover(String name) {
        List<DataSourceProperties> propertiesList = new ArrayList<>();

        // master
        DataSourceProperties masterProperties = discoverMaster(name);
        if (masterProperties != null) {
            propertiesList.add(masterProperties);
        }

        // slave
        propertiesList.addAll(discoverSlaves(name));
        return propertiesList;
    }

    private static DataSourceProperties discoverMaster(String name) {
        List<URL> urls = getResourceUrls(name, JdbcConstants.MASTER);
        if (CollectionUtils.isEmpty(urls)) {
            return null;
        }
        return toDataSourceProperties(name, urls.get(0), true);
    }

    private static List<DataSourceProperties> discoverSlaves(String name) {
        List<URL> urls = getResourceUrls(name, JdbcConstants.REPLICA);
        if (CollectionUtils.isEmpty(urls)) {
            return Collections.emptyList();
        }

        return urls.stream()
                   .map(url -> toDataSourceProperties(name, url, false))
                   .collect(toList());
    }

    private static List<URL> getResourceUrls(String name, String type) {
        List<URL> urls = getUrlsByEnv(name, type);
        if (CollectionUtils.isEmpty(urls)) {
            urls = getUrlsByEnv(name, type);
        }
        if (CollectionUtils.isEmpty(urls)) {
            urls = getUrlsByApollo(name, type);
        }
        return urls;
    }

    private static List<URL> getUrlsByEnv(String name, String type) {
        return getUrlsByFormat(System::getenv, name, type);
    }

    private static List<URL> getUrlsByApollo(String name, String type) {
        IConfigService configService = SpiServiceLoader.get(IConfigService.class).orElse(null);
        if (configService == null) {
            return Collections.emptyList();
        }

        UnaryOperator<String> apolloGetter = key -> configService.getAppConfig().getProperty(key, "");
        return getUrlsByFormat(apolloGetter, name, type);
    }

    private static List<URL> getUrlsByFormat(UnaryOperator<String> getter, String name, String type) {
        List<URL> urls = new ArrayList<>();
        int index = 0;
        while (true) {
            String key = String.format(MYSQL_FMT, name, type, index);
            key = key.toUpperCase().replace('-', '_');

            String value = getter.apply(key);

            if (StringUtils.isBlank(value)) {
                break;
            }

            if (!value.contains("://")) {
                value = String.format("http://%s", value);
            }
            try {
                urls.add(new URL(value));
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
            index++;
        }

        return urls;
    }

}
