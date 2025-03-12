package com.zhihu.fust.spring.toolkit.beans;

import com.zhihu.fust.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.*;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import javax.annotation.Nullable;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * copy bean properties from source to target
 * 1. String -> Int Number (empty or null string will convert to 0)
 * 2. Number -> String, use String.valueOf to convert
 * 3. long->int, int->short, will check max value overflow and throw error
 * 3. short->int, int -> long
 */
public class BeanCopyUtils {
    private static final Logger logger = LoggerFactory.getLogger(BeanCopyUtils.class);
    private static final List<Class<?>> NUMBER_CLASSES = Arrays.asList(long.class, Long.class,
            int.class, Integer.class,
            short.class, Short.class);

    @SuppressWarnings("unchecked")
    private static <T> T newInstance(Class<?> targetClass) {
        try {
            return (T) targetClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }


    public static <E, T> T copyTo(E src, Class<T> targetClass, @Nullable String... ignoreProperties) {
        return toType(targetClass, ignoreProperties).apply(src);
    }

    /**
     * use BeanConverter.toType
     */
    @Deprecated
    public static <E, T> Function<E, T> toType(Class<T> targetClass, @Nullable String... ignoreProperties) {
        return (E source) -> {
            T target = newInstance(targetClass);
            copyProperties(source, target, ignoreProperties);
            return target;
        };

    }

    private static PropertyDescriptor getPropertyDescriptor(Class<?> source, String fieldName) {
        return BeanUtils.getPropertyDescriptor(source, fieldName);
    }


    public static void copyProperties(Object source, Object target, @Nullable String... ignoreProperties) throws BeansException {

        Assert.notNull(source, "Source must not be null");
        Assert.notNull(target, "Target must not be null");

        PropertyDescriptor[] targetPds = BeanUtils.getPropertyDescriptors(target.getClass());
        List<String> ignoreList = (ignoreProperties != null ? Arrays.asList(ignoreProperties) : null);
        for (PropertyDescriptor targetPd : targetPds) {
            Method writeMethod = targetPd.getWriteMethod();
            boolean couldCopy = writeMethod != null && (ignoreList == null || !ignoreList.contains(targetPd.getName()));
            if (couldCopy) {
                PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), targetPd.getName());
                if (sourcePd != null) {
                    doCopyProperty(sourcePd, targetPd, writeMethod, source, target);
                }
            }
        }
    }

    public static <E, T> void copyNonNullProperties(E src, T target, @Nullable String... ignoreProperties) {
        String[] ignoreProps = ArrayUtils.addAll(ignoreProperties, getNullPropertyNames(src));
        copyProperties(src, target, ignoreProps);
    }


    private static void doCopyProperty(PropertyDescriptor sourcePd, PropertyDescriptor targetPd,
                                       Method writeMethod, Object source, Object target) {
        Method readMethod = sourcePd.getReadMethod();
        Class<?> writeParameterType = writeMethod.getParameterTypes()[0];
        boolean isAssignable = ClassUtils.isAssignable(writeParameterType, readMethod.getReturnType());
        Class<?> valueType = readMethod.getReturnType();
        try {
            if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                readMethod.setAccessible(true);
            }
            Object value = readMethod.invoke(source);
            if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                writeMethod.setAccessible(true);
            }
            if (!isAssignable && value != null) {
                if (isNumber(writeParameterType) && isNumber(valueType)) {
                    value = castNumber(writeParameterType, value);
                } else {
                    // String <-> Long/Integer 自动适配转换
                    if (String.class.isAssignableFrom(writeParameterType)) {
                        // 其他类型 -> string
                        value = String.valueOf(value);
                    } else if (value.getClass().isAssignableFrom(String.class)) {
                        // string -> long/int, "" -> 0
                        Object numberValue = toNumber(writeParameterType, value + "");
                        if (numberValue != null) {
                            value = numberValue;
                        }
                    }
                }
            }
            if (value != null) {
                writeMethod.invoke(target, value);
            }

        } catch (Throwable ex) {
            throw new FatalBeanException(
                    "Could not copy property '" + targetPd.getName() + "' from source to target", ex);
        }
    }

    private static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<String>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    private static boolean isLong(Class<?> type) {
        return Long.class.isAssignableFrom(type) || long.class.isAssignableFrom(type);
    }

    private static boolean isInteger(Class<?> type) {
        return Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type);
    }

    private static boolean isShort(Class<?> type) {
        return Short.class.isAssignableFrom(type) || short.class.isAssignableFrom(type);
    }

    private static boolean isNumber(Class<?> type) {
        return NUMBER_CLASSES.stream().anyMatch(x -> x.isAssignableFrom(type));
    }

    private static Number castNumber(Class<?> writeParameterType, Object value) {
        Number num = (Number) value;
        if (isLong(writeParameterType)) {
            // target is long
            return num.longValue();
        } else if (isInteger(writeParameterType)) {
            if (num.longValue() > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("src|" + value + "is overflow, dst|int");
            }
            return num.intValue();

        } else if (isShort(writeParameterType)) {
            if (num.longValue() > Short.MAX_VALUE) {
                throw new IllegalArgumentException("src|" + value + "is overflow, dst|short");
            }
            return num.shortValue();
        }
        return num;
    }

    private static Number toNumber(Class<?> type, String v) {
        if (NumberUtils.isParsable(v)) {
            if (isLong(type)) {
                return Long.parseLong(v);
            }
            if (isInteger(type)) {
                return Integer.parseInt(v);
            }
            if (isShort(type)) {
                return Short.parseShort(v);
            }
        } else {
            if (isNumber(type) && StringUtils.isEmpty(v)) {
                return 0;
            }
        }
        return null;
    }

}
