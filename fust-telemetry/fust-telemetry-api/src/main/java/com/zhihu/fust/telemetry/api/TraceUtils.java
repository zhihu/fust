package com.zhihu.fust.telemetry.api;

import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;

import java.util.concurrent.ThreadLocalRandom;

public final class TraceUtils {
    private TraceUtils() {

    }

    /**
     * check if the context has traceId, if not, create a new traceId
     *
     * @return context with traceId
     */
    public static Context ensureTraceId() {
        Span currentSpan = Span.fromContext(Context.current());
        if (currentSpan.getSpanContext().isValid()) {
            return Context.current();
        }

        // create a new context with traceId
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String traceId = TraceId.fromLongs(random.nextLong(), random.nextLong());
        SpanContext spanContext = createSpanContext(traceId);
        return Context.current().with(Span.wrap(spanContext));
    }

    public static Context attacheTraceId(String traceId) {
        Span currentSpan = Span.fromContext(Context.current());
        if (currentSpan.getSpanContext().isValid()) {
            return Context.current();
        }
        SpanContext spanContext = createSpanContext(traceId);
        // create a new context with traceId
        return Context.current().with(Span.wrap(spanContext));
    }

    /**
     * get traceId from current context
     *
     * @return traceId string value of current context traceId
     */
    public static String getTraceId() {
        Span currentSpan = Span.fromContext(Context.current());
        return currentSpan.getSpanContext().getTraceId();
    }

    private static SpanContext createSpanContext(String traceId) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String spanId = SpanId.fromLong(random.nextLong());
        return SpanContext.create(traceId, spanId, getTraceFlags(), TraceState.getDefault());
    }

    private static TraceFlags getTraceFlags() {
        Span currentSpan = Span.fromContext(Context.current());
        if (currentSpan.getSpanContext().isSampled()) {
            return TraceFlags.getSampled();
        }
        return TraceFlags.getDefault();
    }

}
