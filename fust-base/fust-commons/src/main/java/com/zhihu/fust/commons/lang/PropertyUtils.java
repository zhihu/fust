package com.zhihu.fust.commons.lang;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * get prop from system properties or system env
 */
public final class PropertyUtils {
    private PropertyUtils() {
    }

    /**
     * get property from System Property or Env
     *
     * @param propName prop name
     * @return prop value
     */
    @Nullable
    public static String getProperty(final String propName) {
        final String value = System.getProperty(propName);
        if (StringUtils.isNotEmpty(value)) {
            return value;
        }
        return System.getenv(propName);
    }

    /**
     * get the property and try to convert it to int
     *
     * @param propName prop name
     * @return Optional prop value
     */
    public static Optional<Integer> getIntProperty(final String propName) {
        return NumberUtils.parseInt(getProperty(propName));
    }

    /**
     * get the property and try to convert it to bool
     *
     * @param propName prop name
     * @return Optional prop value
     */
    public static Optional<Boolean> getBoolProperty(final String propName) {
        final String value = getProperty(propName);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of("true".equalsIgnoreCase(value));
    }
}
