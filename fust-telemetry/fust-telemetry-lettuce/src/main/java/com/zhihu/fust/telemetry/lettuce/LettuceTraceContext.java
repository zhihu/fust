package com.zhihu.fust.telemetry.lettuce;

import com.zhihu.fust.telemetry.api.ServiceEntry;

import io.lettuce.core.tracing.TraceContext;
import io.opentelemetry.context.Context;

/**
 * lettuce trace context
 * when start a new span, you can pass this context
 * <p>
 * NOTICE:
 * You can't use ThreadLocal to pass parent span,
 * because the span.start and span.finish is not in the same thread.
 * <p>
 *
 * @author yanzhuzhu
 * @date 2020-01-15
 */
public class LettuceTraceContext implements TraceContext {
    private final Context parent;
    private final ServiceEntry serviceEntry;

    public LettuceTraceContext(Context span) {
        parent = span;
        serviceEntry = span.get(ServiceEntry.SERVICE_ENTRY_KEY);
    }

    public Context getParent() {
        return parent;
    }

    public ServiceEntry getServiceEntry() {
        return serviceEntry;
    }
}
