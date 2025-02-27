package com.zhihu.fust.spring.jdbc;

public interface DataSourceFileProvider {
    DataSourceFileProvider EMPTY = () -> "";

    String getDataSourceFile();
}
