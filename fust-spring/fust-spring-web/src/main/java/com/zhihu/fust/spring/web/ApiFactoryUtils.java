package com.zhihu.fust.spring.web;


import com.zhihu.fust.spring.web.annotation.EnableApiFactory;
import com.zhihu.fust.spring.web.api.ApiFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yanzhuzhu
 * @since 10/08/2018
 */
public class ApiFactoryUtils {
    // 保存创建 ApiFactory 类型，一般数量比较少
    private static ConcurrentHashMap<String, ApiFactory> factoryMap = new ConcurrentHashMap<>();
    // 保存使用 EnableApiFactory 注解控制类的 ApiFactory 实例
    private static ConcurrentHashMap<String, ApiFactory> controllerFactoryMap = new ConcurrentHashMap<>();

    public static ApiFactory getApiFactory(Class<?> containingClass) {
        ApiFactory apiFactory = controllerFactoryMap.get(containingClass.getName());
        if (apiFactory != null) {
            return apiFactory;
        }

        EnableApiFactory enableApiFactory = containingClass.getAnnotation(EnableApiFactory.class);
        Class<? extends ApiFactory> factoryClass = enableApiFactory.value();
        try {
            ApiFactory factory = factoryMap.get(factoryClass.getName());
            if (factory == null) { // 没有实例对象，创建新的实例对象
                factory = factoryClass.newInstance();
                factoryMap.put(factoryClass.getName(), factory);
            }
            controllerFactoryMap.put(containingClass.getName(), factory); // 保存到 factoryMap
            return factory;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("create api factory error:" + factoryClass.getName());
        }

    }
}
