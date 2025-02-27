package com.zhihu.fust.commons.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class StreamKit {

    public static <E, R> Map<R, List<E>> groupingBy(List<E> list, Function<E, R> classifier) {
        return list.stream().collect(Collectors.groupingBy(classifier, Collectors.toList()));
    }

    public static <E> List<E> filter(List<E> list, Predicate<E> predicate) {
        return list.stream().filter(predicate).collect(Collectors.toList());
    }

    public static <E, R> List<R> toList(Collection<E> collection, Function<E, R> mapper) {
        if (isEmpty(collection)) {
            return Collections.emptyList();
        }
        return collection.stream().map(mapper).collect(Collectors.toList());
    }

    public static <E, R> List<R> toListDistinct(Collection<E> collection, Function<E, R> mapper) {
        if (isEmpty(collection)) {
            return Collections.emptyList();
        }
        return collection.stream().map(mapper).distinct().collect(Collectors.toList());
    }

    public static <E, R> Set<R> toSet(Collection<E> collection, Function<E, R> mapper) {
        if (isEmpty(collection)) {
            return Collections.emptySet();
        }
        return collection.stream().map(mapper).collect(Collectors.toSet());
    }

    public static <K, V> Map<K, V> toMap(Collection<V> collection, Function<V, K> keyExtractor) {
        if (isEmpty(collection)) {
            return Collections.emptyMap();
        }
        return collection.stream().collect(Collectors.toMap(keyExtractor, Function.identity()));
    }

    public static <K, V> Map<K, V> toMapWithKeyOverlap(Collection<V> collection, Function<V, K> keyExtractor) {
        if (isEmpty(collection)) {
            return Collections.emptyMap();
        }
        Map<K, V> ret = new HashMap<>(collection.size());
        collection.forEach(x -> ret.put(keyExtractor.apply(x), x));
        return ret;
    }

    private static <E> boolean isEmpty(@Nullable Collection<E> collection) {
        return collection == null || collection.isEmpty();
    }
}
