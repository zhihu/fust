package com.zhihu.fust.commons.collections;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StreamKitTest {
    @Test
    void testGroupingBy() {
        List<String> list = Arrays.asList("apple", "banana", "apricot", "blueberry");
        Map<Character, List<String>> result = StreamKit.groupingBy(list, s -> s.charAt(0));
        assertEquals(2, result.size());
        assertEquals(Arrays.asList("apple", "apricot"), result.get('a'));
        assertEquals(Arrays.asList("banana", "blueberry"), result.get('b'));
    }

    @Test
    void testFilter() {
        List<String> list = Arrays.asList("apple", "banana", "apricot", "blueberry");
        List<String> result = StreamKit.filter(list, s -> s.startsWith("a"));
        assertEquals(Arrays.asList("apple", "apricot"), result);
    }

    @Test
    void testToList() {
        Collection<String> collection = Arrays.asList("apple", "banana", "apricot", "blueberry");
        List<Integer> result = StreamKit.toList(collection, String::length);
        assertEquals(Arrays.asList(5, 6, 7, 9), result);
    }

    @Test
    void testToListWithEmptyCollection() {
        Collection<String> collection = Collections.emptyList();
        List<Integer> result = StreamKit.toList(collection, String::length);
        assertTrue(result.isEmpty());
    }

    @Test
    void testToListDistinct() {
        Collection<String> collection = Arrays.asList("apple", "banana", "apricot", "blueberry", "apple");
        List<String> result = StreamKit.toListDistinct(collection, s -> s);
        assertEquals(Arrays.asList("apple", "banana", "apricot", "blueberry"), result);
    }

    @Test
    void testToListDistinctWithEmptyCollection() {
        Collection<String> collection = Collections.emptyList();
        List<String> result = StreamKit.toListDistinct(collection, s -> s);
        assertTrue(result.isEmpty());
    }

    @Test
    void testToSet() {
        Collection<String> collection = Arrays.asList("apple", "banana", "apricot", "blueberry");
        Set<Integer> result = StreamKit.toSet(collection, String::length);
        assertEquals(new HashSet<>(Arrays.asList(5, 6, 7, 9)), result);
    }

    @Test
    void testToSetWithEmptyCollection() {
        Collection<String> collection = Collections.emptyList();
        Set<Integer> result = StreamKit.toSet(collection, String::length);
        assertTrue(result.isEmpty());
    }

    @Test
    void testToMap() {
        Collection<String> collection = Arrays.asList("apple", "banana", "apricot", "blueberry");
        Map<Integer, String> result = StreamKit.toMap(collection, String::length);
        assertEquals(4, result.size());
        assertEquals("apple", result.get(5));
        assertEquals("banana", result.get(6));
        assertEquals("apricot", result.get(7));
        assertEquals("blueberry", result.get(9));
    }

    @Test
    void testToMapWithEmptyCollection() {
        Collection<String> collection = Collections.emptyList();
        Map<Integer, String> result = StreamKit.toMap(collection, String::length);
        assertTrue(result.isEmpty());
    }

    @Test
    void testToMapWithKeyOverlap() {
        Collection<String> collection = Arrays.asList("apple", "banana", "apricot", "blueberry");
        Map<Character, String> result = StreamKit.toMapWithKeyOverlap(collection, s -> s.charAt(0));
        assertEquals(2, result.size());
        assertEquals("apricot", result.get('a'));
        assertEquals("blueberry", result.get('b'));
    }

    @Test
    void testToMapWithKeyOverlapWithEmptyCollection() {
        Collection<String> collection = Collections.emptyList();
        Map<Character, String> result = StreamKit.toMapWithKeyOverlap(collection, s -> s.charAt(0));
        assertTrue(result.isEmpty());
    }
}