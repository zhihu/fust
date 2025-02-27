package com.zhihu.fust.telemetry.api;

import com.zhihu.fust.commons.lang.SpiServiceLoader;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;

import java.util.Optional;

public class Telemetry {
    private final Tracer tracer;
    private final ServiceMeterCollector collector;

    public Telemetry(String instrumentationName) {
        OpenTelemetry openTelemetry = GlobalOpenTelemetry.get();
        this.tracer = openTelemetry.getTracer(instrumentationName);
        ServiceMeterCollectorFactory factory = SpiServiceLoader.get(ServiceMeterCollectorFactory.class).orElse(name -> new DefaultServiceMeterCollector(openTelemetry, name));
        this.collector = factory.create(instrumentationName);
    }

    public Tracer getTracer() {
        return tracer;
    }

    public static Telemetry create(String instrumentationName) {
        return new Telemetry(instrumentationName);
    }

    public TextMapPropagator getTextMapPropagator() {
        return GlobalOpenTelemetry.getPropagators().getTextMapPropagator();
    }

    public ServiceMeter createServiceMeter(ServiceMeterKind kind) {
        return new ServiceMeter(kind, collector);
    }

    /**
     * Get Service entry from context.
     *
     * @return ServiceEntry
     */
    public static ServiceEntry getServiceEntry() {
        return Optional.ofNullable(Context.current().get(ServiceEntry.SERVICE_ENTRY_KEY))
                .orElse(ServiceEntry.UNKNOWN_ENTRY);
    }
}
