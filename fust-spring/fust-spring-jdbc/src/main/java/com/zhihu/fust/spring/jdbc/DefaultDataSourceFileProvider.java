package com.zhihu.fust.spring.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhihu.fust.commons.lang.StringUtils;

public class DefaultDataSourceFileProvider implements DataSourceFileProvider {
    public static final String DEFAULT_DB_CONFIG_FILE = "classpath:db.json";
    private static final Logger logger = LoggerFactory.getLogger(DataSourceFileProvider.class);

    @Override
    public String getDataSourceFile() {
        String dsFile = System.getProperty("DB_FILE");
        if (StringUtils.isEmpty(dsFile)) {
            return DEFAULT_DB_CONFIG_FILE;
        }
        return dsFile;
    }
}
