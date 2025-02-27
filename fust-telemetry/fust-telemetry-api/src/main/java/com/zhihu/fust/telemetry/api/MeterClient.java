package com.zhihu.fust.telemetry.api;

import com.zhihu.fust.commons.lang.SpiServiceLoader;

public interface MeterClient {
    static MeterClient getMeterClient() {
        return SpiServiceLoader.get(MeterClientProvider.class)
                .map(MeterClientProvider::getMeterClient)
                .orElse(new NoopMeterClient());
    }
    /**
     * Adjusts the specified counter by a given delta.
     *
     * <p>This method is a DataDog extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect the name of the counter to adjust
     * @param delta  the amount to adjust the counter by
     * @param tags   array of tags to be added to the data
     */
    void count(String aspect, long delta, String... tags);

    /**
     * Adjusts the specified counter by a given delta.
     *
     * <p>This method is a DataDog extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect     the name of the counter to adjust
     * @param delta      the amount to adjust the counter by
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void count(String aspect, long delta, double sampleRate, String... tags);

    /**
     * Adjusts the specified counter by a given delta.
     *
     * <p>This method is a DataDog extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect the name of the counter to adjust
     * @param delta  the amount to adjust the counter by
     * @param tags   array of tags to be added to the data
     */
    void count(String aspect, double delta, String... tags);

    /**
     * Adjusts the specified counter by a given delta.
     *
     * <p>This method is a DataDog extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect     the name of the counter to adjust
     * @param delta      the amount to adjust the counter by
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void count(String aspect, double delta, double sampleRate, String... tags);

    /**
     * Increments the specified counter by one.
     *
     * <p>This method is a DataDog extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect the name of the counter to increment
     * @param tags   array of tags to be added to the data
     */
    void incrementCounter(String aspect, String... tags);

    /**
     * Increments the specified counter by one.
     *
     * <p>This method is a DataDog extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect     the name of the counter to increment
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void incrementCounter(String aspect, double sampleRate, String... tags);

    /**
     * Convenience method equivalent to {@link #incrementCounter(String, String[])}.
     *
     * @param aspect the name of the counter to increment
     * @param tags   array of tags to be added to the data
     */
    void increment(String aspect, String... tags);

    /**
     * Convenience method equivalent to {@link #incrementCounter(String, double, String[])}.
     *
     * @param aspect     the name of the counter to increment
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void increment(String aspect, double sampleRate, String... tags);

    /**
     * Decrements the specified counter by one.
     *
     * <p>This method is a DataDog extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect the name of the counter to decrement
     * @param tags   array of tags to be added to the data
     */
    void decrementCounter(String aspect, String... tags);

    /**
     * Decrements the specified counter by one.
     *
     * <p>This method is a DataDog extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect     the name of the counter to decrement
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void decrementCounter(String aspect, double sampleRate, String... tags);

    /**
     * Convenience method equivalent to {@link #decrementCounter(String, String[])}.
     *
     * @param aspect the name of the counter to decrement
     * @param tags   array of tags to be added to the data
     */
    void decrement(String aspect, String... tags);

    /**
     * Convenience method equivalent to {@link #decrementCounter(String, double, String[])}.
     *
     * @param aspect     the name of the counter to decrement
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void decrement(String aspect, double sampleRate, String... tags);

    /**
     * Records the latest fixed value for the specified named gauge.
     *
     * <p>This method is a DataDog extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect the name of the gauge
     * @param value  the new reading of the gauge
     * @param tags   array of tags to be added to the data
     */
    void recordGaugeValue(String aspect, double value, String... tags);

    /**
     * Records the latest fixed value for the specified named gauge.
     *
     * <p>This method is a DataDog extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect     the name of the gauge
     * @param value      the new reading of the gauge
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void recordGaugeValue(String aspect, double value, double sampleRate, String... tags);

    /**
     * Records the latest fixed value for the specified named gauge.
     *
     * <p>This method is a DataDog extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect the name of the gauge
     * @param value  the new reading of the gauge
     * @param tags   array of tags to be added to the data
     */
    void recordGaugeValue(String aspect, long value, String... tags);

    /**
     * Records the latest fixed value for the specified named gauge.
     *
     * <p>This method is a DataDog extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect     the name of the gauge
     * @param value      the new reading of the gauge
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void recordGaugeValue(String aspect, long value, double sampleRate, String... tags);

    /**
     * Convenience method equivalent to {@link #recordGaugeValue(String, double, String[])}.
     *
     * @param aspect the name of the gauge
     * @param value  the new reading of the gauge
     * @param tags   array of tags to be added to the data
     */
    void gauge(String aspect, double value, String... tags);

    /**
     * Convenience method equivalent to {@link #recordGaugeValue(String, double, double, String[])}.
     *
     * @param aspect     the name of the gauge
     * @param value      the new reading of the gauge
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void gauge(String aspect, double value, double sampleRate, String... tags);

    /**
     * Convenience method equivalent to {@link #recordGaugeValue(String, long, String[])}.
     *
     * @param aspect the name of the gauge
     * @param value  the new reading of the gauge
     * @param tags   array of tags to be added to the data
     */
    void gauge(String aspect, long value, String... tags);

    /**
     * Convenience method equivalent to {@link #recordGaugeValue(String, long, double, String[])}.
     *
     * @param aspect     the name of the gauge
     * @param value      the new reading of the gauge
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void gauge(String aspect, long value, double sampleRate, String... tags);

    /**
     * Records an execution time in milliseconds for the specified named operation.
     *
     * <p>This method is a DataDog extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect   the name of the timed operation
     * @param timeInMs the time in milliseconds
     * @param tags     array of tags to be added to the data
     */
    void recordExecutionTime(String aspect, long timeInMs, String... tags);

    /**
     * Records an execution time in milliseconds for the specified named operation.
     *
     * <p>This method is a DataDog extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect     the name of the timed operation
     * @param timeInMs   the time in milliseconds
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void recordExecutionTime(String aspect, long timeInMs, double sampleRate, String... tags);

    /**
     * Convenience method equivalent to {@link #recordExecutionTime(String, long, String[])}.
     *
     * @param aspect the name of the timed operation
     * @param value  the time in milliseconds
     * @param tags   array of tags to be added to the data
     */
    void time(String aspect, long value, String... tags);

    /**
     * Convenience method equivalent to {@link #recordExecutionTime(String, long, double, String[])}.
     *
     * @param aspect     the name of the timed operation
     * @param value      the time in milliseconds
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void time(String aspect, long value, double sampleRate, String... tags);

    /**
     * Records a value for the specified named histogram.
     *
     * <p>This method is a DataDog extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect the name of the histogram
     * @param value  the value to be incorporated in the histogram
     * @param tags   array of tags to be added to the data
     */
    void recordHistogramValue(String aspect, double value, String... tags);

    /**
     * Records a value for the specified named histogram.
     *
     * <p>This method is a DataDog extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect     the name of the histogram
     * @param value      the value to be incorporated in the histogram
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void recordHistogramValue(String aspect, double value, double sampleRate, String... tags);

    /**
     * Records a value for the specified named histogram.
     *
     * <p>This method is a DataDog extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect the name of the histogram
     * @param value  the value to be incorporated in the histogram
     * @param tags   array of tags to be added to the data
     */
    void recordHistogramValue(String aspect, long value, String... tags);

    /**
     * Records a value for the specified named histogram.
     *
     * <p>This method is a DataDog extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect     the name of the histogram
     * @param value      the value to be incorporated in the histogram
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void recordHistogramValue(String aspect, long value, double sampleRate, String... tags);

    /**
     * Convenience method equivalent to {@link #recordHistogramValue(String, double, String[])}.
     *
     * @param aspect the name of the histogram
     * @param value  the value to be incorporated in the histogram
     * @param tags   array of tags to be added to the data
     */
    void histogram(String aspect, double value, String... tags);

    /**
     * Convenience method equivalent to {@link #recordHistogramValue(String, double, double, String[])}.
     *
     * @param aspect     the name of the histogram
     * @param value      the value to be incorporated in the histogram
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void histogram(String aspect, double value, double sampleRate, String... tags);

    /**
     * Convenience method equivalent to {@link #recordHistogramValue(String, long, String[])}.
     *
     * @param aspect the name of the histogram
     * @param value  the value to be incorporated in the histogram
     * @param tags   array of tags to be added to the data
     */
    void histogram(String aspect, long value, String... tags);

    /**
     * Convenience method equivalent to {@link #recordHistogramValue(String, long, double, String[])}.
     *
     * @param aspect     the name of the histogram
     * @param value      the value to be incorporated in the histogram
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void histogram(String aspect, long value, double sampleRate, String... tags);

    /**
     * Records a value for the specified named distribution.
     *
     * <p>This method is a DataDog extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * <p>This is a beta feature and must be enabled specifically for your organization.</p>
     *
     * @param aspect the name of the distribution
     * @param value  the value to be incorporated in the distribution
     * @param tags   array of tags to be added to the data
     */
    void recordDistributionValue(String aspect, double value, String... tags);

    /**
     * Records a value for the specified named distribution.
     *
     * <p>This method is a DataDog extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * <p>This is a beta feature and must be enabled specifically for your organization.</p>
     *
     * @param aspect     the name of the distribution
     * @param value      the value to be incorporated in the distribution
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void recordDistributionValue(String aspect, double value, double sampleRate, String... tags);

    /**
     * Records a value for the specified named distribution.
     *
     * <p>This method is a DataDog extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * <p>This is a beta feature and must be enabled specifically for your organization.</p>
     *
     * @param aspect the name of the distribution
     * @param value  the value to be incorporated in the distribution
     * @param tags   array of tags to be added to the data
     */
    void recordDistributionValue(String aspect, long value, String... tags);

    /**
     * Records a value for the specified named distribution.
     *
     * <p>This method is a DataDog extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * <p>This is a beta feature and must be enabled specifically for your organization.</p>
     *
     * @param aspect     the name of the distribution
     * @param value      the value to be incorporated in the distribution
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void recordDistributionValue(String aspect, long value, double sampleRate, String... tags);

    /**
     * Convenience method equivalent to {@link #recordDistributionValue(String, double, String[])}.
     *
     * @param aspect the name of the distribution
     * @param value  the value to be incorporated in the distribution
     * @param tags   array of tags to be added to the data
     */
    void distribution(String aspect, double value, String... tags);

    /**
     * Convenience method equivalent to {@link #recordDistributionValue(String, double, double, String[])}.
     *
     * @param aspect     the name of the distribution
     * @param value      the value to be incorporated in the distribution
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void distribution(String aspect, double value, double sampleRate, String... tags);

    /**
     * Convenience method equivalent to {@link #recordDistributionValue(String, long, String[])}.
     *
     * @param aspect the name of the distribution
     * @param value  the value to be incorporated in the distribution
     * @param tags   array of tags to be added to the data
     */
    void distribution(String aspect, long value, String... tags);

    /**
     * Convenience method equivalent to {@link #recordDistributionValue(String, long, double, String[])}.
     *
     * @param aspect     the name of the distribution
     * @param value      the value to be incorporated in the distribution
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void distribution(String aspect, long value, double sampleRate, String... tags);

    /**
     * Records a value for the specified set.
     *
     * <p>Sets are used to count the number of unique elements in a group. If you want to track the number of
     * unique visitor to your site, sets are a great way to do that.</p>
     *
     * <p>This method is a DataDog extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect the name of the set
     * @param value  the value to track
     * @param tags   array of tags to be added to the data
     * @see <a href="http://docs.datadoghq.com/guides/dogstatsd/#sets">http://docs.datadoghq.com/guides/dogstatsd/#sets</a>
     */
    void recordSetValue(String aspect, String value, String... tags);

    default void close() {
    }
}
