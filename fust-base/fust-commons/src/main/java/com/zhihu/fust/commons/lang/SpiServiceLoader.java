package com.zhihu.fust.commons.lang;

import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * java spi service loader
 */
public final class SpiServiceLoader {
    private static final Map<Class<?>, Object> CACHE = new ConcurrentHashMap<>(16);

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> get(Class<T> clazz) {
        T service = (T) CACHE.get(clazz);
        if (service != null) {
            return Optional.of(service);
        }

        // synchronize for load
        synchronized (CACHE) {
            ServiceLoader<T> managers = ServiceLoader.load(clazz);
            if (managers.iterator().hasNext()) {
                service = managers.iterator().next();
                CACHE.put(clazz, service);
                return Optional.of(service);
            }
            return Optional.empty();
        }
    }
}
