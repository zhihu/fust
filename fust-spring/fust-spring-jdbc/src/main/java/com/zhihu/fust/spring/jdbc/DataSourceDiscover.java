package com.zhihu.fust.spring.jdbc;

import java.util.List;

import com.zhihu.fust.spring.jdbc.config.DataSourceProperties;

public interface DataSourceDiscover {
    List<DataSourceProperties> discover(String name);
}
