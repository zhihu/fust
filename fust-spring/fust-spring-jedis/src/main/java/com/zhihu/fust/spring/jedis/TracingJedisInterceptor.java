package com.zhihu.fust.spring.jedis;

import com.zhihu.fust.telemetry.api.ServiceEntry;
import com.zhihu.fust.telemetry.api.ServiceMeter;
import com.zhihu.fust.telemetry.api.ServiceMeterKind;
import com.zhihu.fust.telemetry.api.Telemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.semconv.SemanticAttributes;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import redis.clients.jedis.HostAndPort;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class TracingJedisInterceptor implements MethodInterceptor {
    private static final Telemetry TELEMETRY = Telemetry.create("jedis");
    private static final String FMT = "redis_%s_%d";

    private final HostAndPort hostAndPort;

    public TracingJedisInterceptor(@Nonnull HostAndPort hostAndPort) {
        this.hostAndPort = hostAndPort;
    }

    @Nullable
    @Override
    public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
        String serviceName = String.format(FMT, hostAndPort.getHost(), hostAndPort.getPort());
        String operationName = invocation.getMethod().getName();
        Span span = createSpan(serviceName, operationName);
        ServiceMeter serviceMeter = createServiceMeter(serviceName, operationName);
        try {
            return invocation.proceed();
        } catch (Exception e) {
            span.recordException(e);
            serviceMeter.setError(e);
            throw e;
        } finally {
            span.end();
            serviceMeter.end();
        }
    }


    private ServiceMeter createServiceMeter(String serviceName, String operationName) {
        ServiceEntry serviceEntry = Optional.ofNullable(Context.current().get(ServiceEntry.SERVICE_ENTRY_KEY))
                .orElse(ServiceEntry.UNKNOWN_ENTRY);
        ServiceMeter serviceMeter = TELEMETRY.createServiceMeter(ServiceMeterKind.CLIENT);
        serviceMeter.setMethod(serviceEntry.getEntry());
        serviceMeter.setTargetService(serviceName);
        serviceMeter.setTargetMethod(operationName);
        return serviceMeter;
    }

    private Span createSpan(String serviceName, String operationName) {
        Tracer tracer = TELEMETRY.getTracer();

        String spanName = serviceName + '_' + operationName;
        return tracer.spanBuilder(spanName)
                .setSpanKind(SpanKind.CLIENT)
                .setAttribute(SemanticAttributes.PEER_SERVICE, serviceName)
                .setAttribute(SemanticAttributes.DB_SYSTEM, "Redis")
                .setAttribute(SemanticAttributes.DB_OPERATION, operationName)
                .setAttribute(SemanticAttributes.DB_CONNECTION_STRING, serviceName)
                .startSpan();


    }
}
