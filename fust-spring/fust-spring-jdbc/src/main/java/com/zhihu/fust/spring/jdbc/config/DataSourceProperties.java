package com.zhihu.fust.spring.jdbc.config;

import static com.zhihu.fust.spring.jdbc.config.JdbcConstants.MASTER;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSourceProperties {

    private String name;

    /**
     * master or replica
     */
    private String type;

    private String url;
    private String username;
    private String password;

    public DataSourceProperties() {
        // default is master
        this.type = MASTER;
    }

    public boolean isMaster() {
        return MASTER.equals(type);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
