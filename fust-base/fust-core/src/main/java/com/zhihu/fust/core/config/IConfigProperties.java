package com.zhihu.fust.core.config;

/**
 * java config properties
 */
public interface IConfigProperties {
    /**
     * Return the property value with the given key, or {@code defaultValue} if the key doesn't exist.
     *
     * @param key          the property name
     * @param defaultValue the default value when key is not found or any error occurred
     * @return the property value
     */
    String getProperty(String key, String defaultValue);

    /**
     * Return the integer property value with the given key, or {@code defaultValue} if the key
     * doesn't exist.
     *
     * @param key          the property name
     * @param defaultValue the default value when key is not found or any error occurred
     * @return the property value as integer
     */
    Integer getIntProperty(String key, Integer defaultValue);

    /**
     * Return the long property value with the given key, or {@code defaultValue} if the key doesn't
     * exist.
     *
     * @param key          the property name
     * @param defaultValue the default value when key is not found or any error occurred
     * @return the property value as long
     */
    Long getLongProperty(String key, Long defaultValue);

    /**
     * Return the double property value with the given key, or {@code defaultValue} if the key doesn't
     * exist.
     *
     * @param key          the property name
     * @param defaultValue the default value when key is not found or any error occurred
     * @return the property value as double
     */
    Double getDoubleProperty(String key, Double defaultValue);

    void addChangeListener(IConfigPropertiesChangeListener listener);
}
