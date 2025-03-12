package com.zhihu.fust.commons.collections;

import java.util.HashMap;
import java.util.Map;

public final class MapBuilder<K, V> {
    private Map<K, V> data;

    private MapBuilder(K key, V value) {
        data = new HashMap<>();
        data.put(key, value);
    }

    public static <K, V> MapBuilder<K, V> of(K key, V value) {
        return new MapBuilder<>(key, value);
    }

    public MapBuilder<K, V> put(K key, V value) {
        data.put(key, value);
        return this;
    }

    public Map<K, V> build() {
        return data;
    }
}