package com.zhihu.fust.commons.io.resource;


import com.zhihu.fust.commons.lang.StringUtils;

import java.net.URL;
import java.util.Optional;

public class URLUser {
    private String password;
    private String username;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public static Optional<URLUser> of(URL url) {
        String userInfoStr = url.getUserInfo();

        if (StringUtils.isEmpty(userInfoStr)) {
            return Optional.empty();
        }

        URLUser user = new URLUser();
        int index = userInfoStr.indexOf(':');
        if (index == -1) { // no password, only user
            user.setUsername(userInfoStr);
        } else {
            user.setUsername(userInfoStr.substring(0, index));
            user.setPassword(userInfoStr.substring(index + 1));
        }
        return Optional.of(user);
    }


}
