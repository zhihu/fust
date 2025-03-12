package com.zhihu.fust.armeria.grpc;

import com.linecorp.armeria.common.logging.RequestLog;
import com.zhihu.fust.commons.lang.PropertyUtils;
import com.zhihu.fust.telemetry.api.ServiceEntry;
import com.zhihu.fust.telemetry.api.ServiceMeter;
import com.zhihu.fust.telemetry.api.ServiceMeterKind;
import com.zhihu.fust.telemetry.api.Telemetry;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public final class GrpcTelemetry {

    /**
     * Switch to track business error codes
     * It is recommended to only enable it in scenarios where error codes need to be viewed.
     * Business errors are typically expected errors. To reduce interference from unexpected errors,
     * it is generally advisable not to enable it in most scenarios.
     */
    public static final String RECORD_GRPC_BIZ_ERROR_KEY = "enable.grpc.biz.error.meter";

    private static final boolean enableGrpcBizErrorMeter;

    static {
        enableGrpcBizErrorMeter = PropertyUtils.getBoolProperty(RECORD_GRPC_BIZ_ERROR_KEY).orElse(false);
    }

    public static boolean enableGrpcBizErrorMeter() {
        return enableGrpcBizErrorMeter;
    }

    private GrpcTelemetry() {
    }

    private static final Logger log = LoggerFactory.getLogger(GrpcTelemetry.class);

    private static final Telemetry TELEMETRY = Telemetry.create("grpc");

    public static void endSpan(Span span) {
        try {
            span.end();
        } catch (Exception e) {
            log.error("end span error|{}", e.getMessage(), e);
        }
    }

    public static void addExceptionEvent(Span span, String error) {
        if (span == null) {
            return;
        }
        span.addEvent("exception", Attributes.of(AttributeKey.stringKey("payload"), error));
    }

    public static Tracer getTracer() {
        return TELEMETRY.getTracer();
    }

    public static ServiceEntry getServiceEntry() {
        return Telemetry.getServiceEntry();
    }

    public static <ReqT, RespT> String getMethodName(MethodDescriptor<ReqT, RespT> method) {
        String serviceName = method.getServiceName();
        int dotPos = Optional.ofNullable(serviceName).map(s -> s.lastIndexOf(".")).orElse(-1);
        if (dotPos > 0) {
            serviceName = serviceName.substring(dotPos + 1);  // remove package name
        }
        return serviceName + "_" + method.getBareMethodName();
    }

    public static String getMethodByRequestLog(RequestLog requestLog) {
        String fullname = requestLog.fullName();
        if (fullname.lastIndexOf(".") != -1) {
            fullname = fullname.substring(fullname.lastIndexOf(".") + 1);
        }
        return fullname.replace("/", "_");
    }

    /**
     * extract parent context
     *
     * @param request http servlet request
     * @return parent context
     */
    public static Context extract(Metadata request) {
        TextMapPropagator textMapPropagator = TELEMETRY.getTextMapPropagator();
        Context extract = textMapPropagator.extract(Context.current(), request, GETTER);
        if (extract == Context.current()) {
            // fail to extract, create a new one
            ThreadLocalRandom random = ThreadLocalRandom.current();
            String traceId = TraceId.fromLongs(random.nextLong(), random.nextLong());
            String spanId = SpanId.fromLong(random.nextLong());
            SpanContext spanContext = SpanContext.create(traceId, spanId, TraceFlags.getSampled(), TraceState.getDefault());
            return Context.current().with(Span.wrap(spanContext));
        }
        return extract;
    }

    public static void inject(Metadata metadata) {
        TextMapPropagator textMapPropagator = TELEMETRY.getTextMapPropagator();
        textMapPropagator.inject(Context.current(), metadata, SETTER);
    }

    private static final TextMapSetter<Metadata> SETTER =
            (metadata, key, value) -> {
                if (metadata != null) {
                    metadata.put(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER), value);
                }
            };

    private static final TextMapGetter<Metadata> GETTER = new TextMapGetter<Metadata>() {
        @Override
        public Iterable<String> keys(Metadata metadata) {
            return metadata.keys();
        }

        @Override
        public String get(Metadata carrier, @Nonnull String key) {
            if (carrier != null) {
                return carrier.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER));
            }
            return null;
        }
    };

    public static ServiceMeter createServiceMeter(ServiceMeterKind kind) {
        return TELEMETRY.createServiceMeter(kind);
    }

    public static TextMapPropagator getTextMapPropagator(TextMapPropagator textMapPropagator) {
        return textMapPropagator;
    }

}
