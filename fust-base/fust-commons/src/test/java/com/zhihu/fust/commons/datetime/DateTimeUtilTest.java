package com.zhihu.fust.commons.datetime;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateTimeUtilTest {

    @Test
    void testFormatWithDate() {
        Date date = new Date();
        String pattern = "yyyy-MM-dd";
        String formattedDate = DateTimeUtil.format(date, pattern);
        assertNotNull(formattedDate);
    }

    @Test
    void testFormatWithLocalDateTime() {
        LocalDateTime dateTime = LocalDateTime.now();
        String pattern = "yyyy-MM-dd HH:mm:ss";
        String formattedDate = DateTimeUtil.format(dateTime, pattern);
        assertNotNull(formattedDate);
    }

    @Test
    void testLocalDateTimeOfDate() {
        Date date = new Date();
        LocalDateTime dateTime = DateTimeUtil.localDateTimeOf(date);
        assertNotNull(dateTime);
    }

    @Test
    void testLocalDateTimeOfLocalDate() {
        LocalDate date = LocalDate.now();
        LocalDateTime dateTime = DateTimeUtil.localDateTimeOf(date);
        assertNotNull(dateTime);
    }

    @Test
    void testLocalDateTimeOfEpochMilli() {
        long epochMilli = System.currentTimeMillis();
        LocalDateTime dateTime = DateTimeUtil.localDateTimeOf(epochMilli);
        assertNotNull(dateTime);
    }

    @Test
    void testLocalDateTimeOfStringAndPattern() {
        String datetime = "2023-10-01 12:00:00";
        String pattern = "yyyy-MM-dd HH:mm:ss";
        LocalDateTime dateTime = DateTimeUtil.localDateTimeOf(datetime, pattern);
        assertNotNull(dateTime);
    }

    @Test
    void testLocalDateTimeOfStringWithDefaultFormatter() {
        String datetime = "2023-10-01 12:00:00";
        LocalDateTime dateTime = DateTimeUtil.localDateTimeOf(datetime);
        assertNotNull(dateTime);
    }

    @Test
    void testLocalDateOfEpochMillis() {
        long epochMillis = System.currentTimeMillis();
        LocalDate date = DateTimeUtil.localDateOf(epochMillis);
        assertNotNull(date);
    }

    @Test
    void testLocalDateOfString() {
        String date = "2023-10-01";
        LocalDate localDate = DateTimeUtil.localDateOf(date);
        assertNotNull(localDate);
    }

    @Test
    void testEpochMilliOfLocalDateTime() {
        LocalDateTime dateTime = LocalDateTime.now();
        long epochMilli = DateTimeUtil.epochMilliOf(dateTime);
        assertTrue(epochMilli > 0);
    }

    @Test
    void testEpochMilliOfLocalDate() {
        LocalDate date = LocalDate.now();
        long epochMilli = DateTimeUtil.epochMilliOf(date);
        assertTrue(epochMilli > 0);
    }

    @Test
    void testEpochMilliOfLocalDateWithDuration() {
        LocalDate date = LocalDate.now();
        Duration duration = Duration.ofDays(1);
        long epochMilli = DateTimeUtil.epochMilliOf(date, duration);
        assertTrue(epochMilli > 0);
    }

    @Test
    void testEpochMilliOfStringAndPattern() {
        String dateTime = "2023-10-01 12:00:00";
        String pattern = "yyyy-MM-dd HH:mm:ss";
        long epochMilli = DateTimeUtil.epochMilliOf(dateTime, pattern);
        assertTrue(epochMilli > 0);
    }

    @Test
    void testEpochMilliOfStringWithDefaultFormatter() {
        String dateTime = "2023-10-01 12:00:00";
        long epochMilli = DateTimeUtil.epochMilliOf(dateTime);
        assertTrue(epochMilli > 0);
    }

    @Test
    void testEpochOfLocalDateTime() {
        LocalDateTime dateTime = LocalDateTime.now();
        long epoch = DateTimeUtil.epochOf(dateTime);
        assertTrue(epoch > 0);
    }

    @Test
    void testEpochOfLocalDateTimeWithDuration() {
        LocalDateTime dateTime = LocalDateTime.now();
        Duration duration = Duration.ofDays(1);
        long epoch = DateTimeUtil.epochOf(dateTime, duration);
        assertTrue(epoch > 0);
    }

    @Test
    void testEpochOfLocalDate() {
        LocalDate date = LocalDate.now();
        long epoch = DateTimeUtil.epochOf(date);
        assertTrue(epoch > 0);
    }

    @Test
    void testEpochOfMonthWithPattern() {
        String month = "2023-10";
        String pattern = "yyyy-MM";
        long epoch = DateTimeUtil.epochOfMonth(month, pattern);
        assertTrue(epoch > 0);
    }

    @Test
    void testEpochOfMonthWithDefaultFormatter() {
        String month = "2023-10";
        long epoch = DateTimeUtil.epochOfMonth(month);
        assertTrue(epoch > 0);
    }

    @Test
    void testGetFormatter() {
        String pattern = "yyyy-MM-dd";
        assertNotNull(DateTimeUtil.getFormatter(pattern));
    }

    @Test
    void testLocalDateTimeOfEpoch() {
        long epoch = System.currentTimeMillis();
        LocalDateTime dateTime = DateTimeUtil.localDateTimeOf(epoch);
        assertNotNull(dateTime);
    }
}