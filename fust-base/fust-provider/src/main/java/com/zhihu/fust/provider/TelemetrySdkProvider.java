package com.zhihu.fust.provider;

import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;

public interface TelemetrySdkProvider {
    SdkTracerProvider sdkTracerProvider();

    SdkMeterProvider sdkMeterProvider();

    SdkLoggerProvider sdkLoggerProvider();

    TextMapPropagator textMapPropagator();
}
