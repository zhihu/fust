package com.zhihu.fust.ai.chat.service;

public class DefaultUserProviderService implements UserProviderService {
    @Override
    public String currentUser() {
        return "default";
    }
}
