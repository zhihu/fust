package com.zhihu.fust.commons.lang;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * number utils
 */
public final class NumberUtils {

    public static final Logger log = LoggerFactory.getLogger(NumberUtils.class);

    private NumberUtils() {
    }

    /**
     * parse value to int
     * if parse error, return empty optional
     *
     * @param value string value
     * @return optional int value
     */
    public static Optional<Integer> parseInt(String value) {
        if (StringUtils.isNumeric(value)) {
            try {
                return Optional.of(Integer.parseInt(value));
            } catch (Exception e) {
                log.error("parseIntError, value|{}", value);
            }
        }
        return Optional.empty();
    }

    /**
     * parse value to long
     * if parse error, return empty optional
     *
     * @param value string value
     * @return optional long value
     */
    public static Optional<Long> parseLong(String value) {
        if (StringUtils.isNumeric(value)) {
            try {
                return Optional.of(Long.parseLong(value));
            } catch (Exception e) {
                log.error("parseLongError, value|{}", value);
            }
        }
        return Optional.empty();
    }
}
