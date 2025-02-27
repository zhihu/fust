package com.zhihu.fust.commons.datetime;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class DateTimeUtil {

    public static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter YYYY_MM = DateTimeFormatter.ofPattern("yyyy-MM");
    public static final DateTimeFormatter YYYY = DateTimeFormatter.ofPattern("yyyy");
    public static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd HH:mm:ss");

    private static final Map<String, DateTimeFormatter> formatterMapCache = new HashMap<>();
    private static final Function<String, DateTimeFormatter> formatterCreator = pattern -> {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        formatterMapCache.put(pattern, formatter);
        return formatter;
    };

    public static String format(Date date, String pattern) {
        return format(localDateTimeOf(date), pattern);
    }

    public static String format(LocalDateTime dateTime, String pattern) {
        return getFormatter(pattern).format(dateTime);
    }

    public static LocalDateTime localDateTimeOf(Date date) {
        return localDateTimeOf(date.getTime());
    }

    public static LocalDateTime localDateTimeOf(LocalDate date) {
        return date.atStartOfDay();
    }

    public static LocalDateTime localDateTimeOf(long epochMilli) {
        return Instant.ofEpochMilli(epochMilli)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static LocalDateTime localDateTimeOfEpoch(long epoch) {
        return Instant.ofEpochMilli(epoch * 1000L)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static LocalDateTime localDateTimeOf(String datetime, String pattern) {
        return localDateTimeOf(datetime, getFormatter(pattern));
    }

    public static LocalDateTime localDateTimeOf(String datetime, DateTimeFormatter formatter) {
        return LocalDateTime.parse(datetime, formatter);
    }

    /**
     * use default formatter yyyy-MM-dd HH:mm:ss
     *
     * @param dateTime dateTime string like 2019-01-30 11:23:59
     */
    public static LocalDateTime localDateTimeOf(String dateTime) {
        return LocalDateTime.parse(dateTime, DEFAULT_FORMATTER);
    }

    public static LocalDate localDateOf(long epochMillis) {
        return localDateTimeOf(epochMillis).toLocalDate();
    }

    public static LocalDate localDateOf(String date) {
        return LocalDate.parse(date, YYYY_MM_DD);
    }

    public static long epochMilliOf(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static long epochMilliOf(LocalDate date) {
        return epochMilliOf(date.atStartOfDay());
    }

    public static long epochMilliOf(LocalDate date, Duration duration) {
        LocalDateTime dateTime = date.atStartOfDay();
        return epochMilliOf(dateTime.plus(duration));
    }

    public static long epochMilliOf(String dateTime, String pattern) {
        return epochMilliOf(localDateTimeOf(dateTime, pattern));
    }

    /***
     * default formatter yyyy-MM-dd HH:mm:ss
     * @param dateTime datetime string like 2019-01-30 11:23:59
     * */
    public static long epochMilliOf(String dateTime) {
        return epochMilliOf(localDateTimeOf(dateTime));
    }

    public static long epochOf(LocalDateTime dateTime) {
        return epochMilliOf(dateTime) / 1000L;
    }

    public static long epochOf(LocalDateTime dateTime, Duration duration) {
        return epochMilliOf(dateTime.plus(duration)) / 1000L;
    }

    public static long epochOf(LocalDate date) {
        return epochMilliOf(date) / 1000L;
    }

    public static long epochOfMonth(String month, String pattern) {
        LocalDate date = YearMonth.parse(month, getFormatter(pattern)).atDay(1);
        return epochMilliOf(date);
    }

    public static long epochOfMonth(String month) {
        LocalDate date = YearMonth.parse(month, YYYY_MM).atDay(1);
        return epochMilliOf(date);
    }

    public static DateTimeFormatter getFormatter(String pattern) {
        return Optional.of(pattern)
                .map(formatterMapCache::get)
                .orElseGet(() -> formatterCreator.apply(pattern));

    }

}
