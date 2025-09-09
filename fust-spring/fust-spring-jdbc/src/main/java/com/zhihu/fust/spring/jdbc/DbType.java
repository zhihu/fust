package com.zhihu.fust.spring.jdbc;

import com.zhihu.fust.commons.lang.StringUtils;

public enum DbType {
    MYSQL,
    POSTGRESQL,
    OTHER;

    public boolean match(String url) {
        if (StringUtils.isEmpty(url)) {
            return false;
        }
        return url.toLowerCase().contains(name().toLowerCase());
    }
}
