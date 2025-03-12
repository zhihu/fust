package com.zhihu.fust.config.extension;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zhihu.fust.core.config.IConfigService;

public interface ConfigClient {
    <T> T get(Class<T> clazz, String namespace, String key);

    <T> List<T> listOf(TypeReference<List<T>> typeReference, String namespace, String key);

    String getString(String namespace, String key);

    Integer getInteger(String namespace, String key);

    Long getLong(String namespace, String key);

    Double getDouble(String namespace, String key);

    /***
     * default namespace 'application'
     */
    <T> T get(Class<T> clazz, String key);

    /***
     * default namespace 'application'
     */
    <T> List<T> listOf(TypeReference<List<T>> typeReference, String key);

    /***
     * default namespace 'application'
     */
    String getString(String key);

    /***
     * default namespace 'application'
     */
    Integer getInteger(String key);

    /***
     * default namespace 'application'
     */
    Long getLong(String key);

    /***
     * default namespace 'application'
     */
    Double getDouble(String key);

    /**
     * get json namespace as object
     *
     * @param clazz     class
     * @param namespace namespace
     * @param <T>       object class
     * @return object
     */
    <T> T getConfigByJsonNamespace(Class<T> clazz, String namespace);

    IConfigService getConfigService();
}
