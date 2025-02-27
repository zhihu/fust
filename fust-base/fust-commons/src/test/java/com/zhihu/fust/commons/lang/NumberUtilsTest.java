package com.zhihu.fust.commons.lang;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NumberUtilsTest {

    @Test
    void parseInt() {
        assertEquals(Optional.empty(), NumberUtils.parseInt(null));
        assertEquals(Optional.empty(), NumberUtils.parseInt(""));
        assertEquals(Optional.of(123), NumberUtils.parseInt("123"));
        assertEquals(Optional.empty(), NumberUtils.parseInt("12 3"));
        assertEquals(Optional.empty(), NumberUtils.parseInt("abc"));
        assertEquals(Optional.empty(), NumberUtils.parseInt("12-3"));
        assertEquals(Optional.empty(), NumberUtils.parseInt("12.3"));
    }

    @Test
    void parseLong() {
        assertEquals(Optional.empty(), NumberUtils.parseLong(null));
        assertEquals(Optional.empty(), NumberUtils.parseLong(""));
        assertEquals(Optional.of(123L), NumberUtils.parseLong("123"));
        assertEquals(Optional.empty(), NumberUtils.parseLong("12 3"));
        assertEquals(Optional.empty(), NumberUtils.parseLong("abc"));
        assertEquals(Optional.empty(), NumberUtils.parseLong("12-3"));
        assertEquals(Optional.empty(), NumberUtils.parseLong("12.3"));
    }
}