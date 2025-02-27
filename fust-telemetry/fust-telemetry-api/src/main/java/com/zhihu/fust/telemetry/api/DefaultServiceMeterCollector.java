package com.zhihu.fust.telemetry.api;

import com.zhihu.fust.commons.lang.StringUtils;
import com.zhihu.fust.core.env.Env;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;

import java.util.Locale;

public class DefaultServiceMeterCollector implements ServiceMeterCollector {
    public static final String TAG_ERROR = "error";
    public static final String PREFIX = "span.";
    public static final String CALL = "call.";
    public static final String COUNT = "count";
    public static final String DURATION = "duration";

    private static final String SERVICE_NAME = Env.getServiceName();
    private final Meter meter;


    public DefaultServiceMeterCollector(OpenTelemetry openTelemetry, String name) {
        // Gets or creates a named meter instance
        meter = openTelemetry.meterBuilder(name)
                .build();
    }

    @Override
    public void collect(ServiceMeter serviceMeter) {
        // It is recommended that the API user keep a reference to Attributes they will record against
        AttributesBuilder builder = Attributes.builder();
        for (String tag : serviceMeter.getTags()) {
            String[] tagValues = tag.split(":");
            builder.put(tagValues[0], tagValues[1]);
        }
        if (serviceMeter.hasError()) {
            builder.put(TAG_ERROR, serviceMeter.getError());
        }
        Attributes attributes = builder.build();

        String aspectName = build(serviceMeter, COUNT, "");
        LongCounter counter = meter
                .counterBuilder(aspectName)
                .build();
        counter.add(1, attributes);

        long delta = System.currentTimeMillis() - serviceMeter.getStartTime();
        meter.histogramBuilder(build(serviceMeter, DURATION, ""))
                .ofLongs()
                .build()
                .record(delta, attributes);

        if (serviceMeter.hasError()) {
            String errorName = build(serviceMeter, COUNT, serviceMeter.getError());
            meter.counterBuilder(errorName)
                    .build()
                    .add(1, attributes);
        }
    }

    /**
     * <p>
     * server side
     * span.server.$service_name.$method.duration
     * span.server.$service_name.$method.count
     * span.server.$service_name.$method.error.$error_name.count
     * <p>
     * client side
     * span.client.$service_name.$method.call.$target_service_name.$target_method.duration
     * span.client.$service_name.$method.call.$target_service_name.$target_method.count
     * span.client.$service_name.$method.call.$target_service_name.$target_method.error.$error_name.count
     */
    public String build(ServiceMeter serviceMeter, String suffix, String errorType) {
        String typeName = serviceMeter.getKind().name().toLowerCase(Locale.ROOT);
        String method = serviceMeter.getMethod();
        StringBuilder sb = new StringBuilder(PREFIX)
                .append(typeName).append('.')
                .append(SERVICE_NAME).append('.')
                .append(method).append('.');

        if (serviceMeter.getKind() == ServiceMeterKind.CLIENT) {
            // for client
            sb.append(CALL)
                    .append(serviceMeter.getTargetService()).append('.')
                    .append(serviceMeter.getTargetMethod()).append('.');
        }

        // for error
        if (StringUtils.isNotEmpty(errorType)) {
            sb.append(TAG_ERROR).append('.').append(errorType).append('.');
        }
        sb.append(suffix);
        return sb.toString();
    }
}
