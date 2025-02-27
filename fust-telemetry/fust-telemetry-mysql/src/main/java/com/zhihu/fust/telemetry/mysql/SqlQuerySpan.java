package com.zhihu.fust.telemetry.mysql;

import com.zhihu.fust.telemetry.api.ServiceMeter;
import io.opentelemetry.api.trace.Span;

final class SqlQuerySpan {
    private final Span span;
    private final ServiceMeter serviceMeter;

    SqlQuerySpan(Span span, ServiceMeter serviceMeter) {
        this.span = span;
        this.serviceMeter = serviceMeter;
    }

    public void end() {
        if (span != null) {
            span.end();
        }
        if (serviceMeter != null) {
            serviceMeter.end();
        }
    }
}
