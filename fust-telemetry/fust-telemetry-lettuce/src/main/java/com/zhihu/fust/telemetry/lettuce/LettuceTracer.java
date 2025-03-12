package com.zhihu.fust.telemetry.lettuce;

import io.lettuce.core.tracing.TraceContext;
import io.opentelemetry.api.trace.Tracer;

/**
 * lettuce tracer
 *
 * @author yanzhuzhu
 * @date 2020-01-15
 */
public class LettuceTracer extends io.lettuce.core.tracing.Tracer {
    private final Tracer tracer;
    private static final String ENABLE_TRACE_REDIS_ARGS = "ENABLE_TRACE_REDIS_ARGS";

    private final boolean includeCommandArgs;

    public LettuceTracer(Tracer tracer) {
        this.tracer = tracer;
        this.includeCommandArgs = isIncludeCommandArgsInSpanTags();
    }

    private static boolean isIncludeCommandArgsInSpanTags() {
        String value = System.getProperty(ENABLE_TRACE_REDIS_ARGS);
        if (value == null) {
            value = System.getenv(ENABLE_TRACE_REDIS_ARGS);
        }
        return Boolean.parseBoolean(value);
    }


    @Override
    public Span nextSpan() {
        return nextSpan(null);
    }

    @Override
    public Span nextSpan(TraceContext traceContext) {
        if (traceContext instanceof LettuceTraceContext) {
            return new LettuceSpan(tracer, (LettuceTraceContext) traceContext, includeCommandArgs);
        }
        return new LettuceSpan(tracer, null, includeCommandArgs);
    }

}
