package com.zhihu.fust.telemetry.sdk;

import java.util.Optional;

import javax.annotation.Nullable;

import com.zhihu.fust.commons.lang.SpiServiceLoader;
import com.zhihu.fust.provider.TelemetrySdkProvider;

import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.OpenTelemetrySdkBuilder;

public class TelemetryInitializer {
    private static volatile boolean initialized = false;

    public static void init() {
        init(null);
    }

    public static void init(@Nullable TelemetrySdkProvider sdkProvider) {
        if (initialized) {
            return;
        }
        initialized = true;

        if (sdkProvider == null) {
            sdkProvider = SpiServiceLoader.get(TelemetrySdkProvider.class)
                                          .orElse(new DefaultTelemetrySdkProvider());
        }

        OpenTelemetrySdkBuilder builder = OpenTelemetrySdk.builder();
        Optional.ofNullable(sdkProvider.sdkTracerProvider()).ifPresent(builder::setTracerProvider);
        Optional.ofNullable(sdkProvider.sdkMeterProvider()).ifPresent(builder::setMeterProvider);
        Optional.ofNullable(sdkProvider.sdkLoggerProvider()).ifPresent(builder::setLoggerProvider);
        Optional.ofNullable(sdkProvider.textMapPropagator()).ifPresent(
                textMapPropagator -> builder.setPropagators(ContextPropagators.create(textMapPropagator)));
        builder.buildAndRegisterGlobal();

    }
}
