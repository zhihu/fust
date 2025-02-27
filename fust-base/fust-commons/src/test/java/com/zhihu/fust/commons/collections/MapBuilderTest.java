package com.zhihu.fust.commons.collections;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MapBuilderTest {

    @Test
    void testOf() {
        MapBuilder<String, Integer> mapBuilder = MapBuilder.of("key1", 1);
        Map<String, Integer> map = mapBuilder.build();
        assertEquals(1, map.size());
        assertEquals(1, map.get("key1"));
    }

    @Test
    void testPut() {
        MapBuilder<String, Integer> mapBuilder = MapBuilder.of("key1", 1);
        mapBuilder.put("key2", 2);
        mapBuilder.put("key3", 3);
        Map<String, Integer> map = mapBuilder.build();
        assertEquals(3, map.size());
        assertEquals(1, map.get("key1"));
        assertEquals(2, map.get("key2"));
        assertEquals(3, map.get("key3"));
    }

    @Test
    void testBuild() {
        MapBuilder<String, Integer> mapBuilder = MapBuilder.of("key1", 1);
        mapBuilder.put("key2", 2);
        Map<String, Integer> map = mapBuilder.build();
        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals(1, map.get("key1"));
        assertEquals(2, map.get("key2"));
    }
}