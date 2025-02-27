package com.zhihu.fust.telemetry.sdk;

import com.zhihu.fust.provider.TelemetrySdkProvider;
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;

public class DefaultTelemetrySdkProvider implements TelemetrySdkProvider {

    public DefaultTelemetrySdkProvider() {
    }

    @Override
    public SdkTracerProvider sdkTracerProvider() {
        return null;
    }

    @Override
    public SdkMeterProvider sdkMeterProvider() {
        return null;
    }

    @Override
    public SdkLoggerProvider sdkLoggerProvider() {
        return null;
    }

    @Override
    public TextMapPropagator textMapPropagator() {
        return TextMapPropagator.composite(W3CTraceContextPropagator.getInstance(), W3CBaggagePropagator.getInstance());
    }
}
