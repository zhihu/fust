package com.zhihu.fust.core.config;

import java.util.stream.Stream;

import com.zhihu.fust.commons.lang.StringUtils;

public enum ConfigFileFormatEnum {
    Properties("properties"),
    XML("xml"),
    JSON("json"),
    YML("yml"),
    YAML("yaml"),
    TXT("txt");

    private final String value;

    ConfigFileFormatEnum(String value) {
        this.value = value;
    }

    private static String getWellFormedName(String configFileName) {
        return StringUtils.isBlank(configFileName) ? "" : configFileName.trim().toLowerCase();
    }

    public String getConfigFileName(String namespace) {
        return namespace + "." + getValue();
    }

    public static ConfigFileFormatEnum fromString(String value) {
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("value can not be empty");
        } else {
            String cleansedName = getWellFormedName(value);
            return (ConfigFileFormatEnum) Stream.of(values()).filter((item) -> {
                return cleansedName.equalsIgnoreCase(item.getValue());
            }).findFirst().orElseThrow(() -> {
                return new IllegalArgumentException(value + " can not map enum");
            });
        }
    }

    public static boolean isValidFormat(String value) {
        try {
            fromString(value);
            return true;
        } catch (IllegalArgumentException var2) {
            return false;
        }
    }

    public static boolean isPropertiesCompatible(ConfigFileFormatEnum format) {
        return format == YAML || format == YML || format == Properties;
    }

    public String getValue() {
        return this.value;
    }
}
