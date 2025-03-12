package com.zhihu.fust.telemetry.spring.mvc;

import com.zhihu.fust.telemetry.api.ServiceEntry;
import com.zhihu.fust.telemetry.api.ServiceMeter;
import com.zhihu.fust.telemetry.api.ServiceMeterKind;
import com.zhihu.fust.telemetry.api.Telemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.semconv.SemanticAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.util.ClassUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;

/**
 * spring mvc telemetry interceptor
 */

public class SpringMvcTelemetryInterceptor implements AsyncHandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(SpringMvcTelemetryInterceptor.class);
    private static final String HTTP_SPAN_SCOPE = "http-span-scope";
    private static final String HTTP_SERVICE_METER = "http-service-meter";

    private static final Telemetry TELEMETRY = Telemetry.create("meter-spring-mvc");

    @Override
    @SuppressWarnings("NullableProblems")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            // ignore options method
            return true;
        }

        try {
            startInstrumentation(request, (HandlerMethod) handler);
        } catch (Exception e) {
            log.warn("error http tracing|{}", e.getMessage(), e);
        }
        return true;
    }

    private static void startInstrumentation(HttpServletRequest request, HandlerMethod handler) {
        String name = handler.getMethod().getName();
        String ctrName = ClassUtils.getUserClass(handler.getBean().getClass()).getSimpleName();
        String operationName = String.format("%s_%s", ctrName, name);
        Span span = createSpan(operationName, request);
        ServiceMeter metric = TELEMETRY.createServiceMeter(ServiceMeterKind.SERVER);
        metric.setMethod(operationName);
        Scope scope = Context.current().with(span)
                .with(ServiceEntry.SERVICE_ENTRY_KEY, ServiceEntry.create(operationName))
                .makeCurrent();
        request.setAttribute(HTTP_SPAN_SCOPE, scope);
        request.setAttribute(HTTP_SERVICE_METER, metric);
    }

    private static final TextMapGetter<HttpServletRequest> GETTER = new TextMapGetter<HttpServletRequest>() {
        @Override
        public Iterable<String> keys(HttpServletRequest carrier) {
            return Collections.list(carrier.getHeaderNames());
        }

        @Override
        public String get(HttpServletRequest carrier, String key) {
            return carrier.getHeader(key);
        }
    };

    private static Span createSpan(String operationName, HttpServletRequest request) {
        Context extractedContext = TELEMETRY.getTextMapPropagator()
                .extract(Context.current(), request, GETTER);
        StringBuffer reqUrl = request.getRequestURL();
        String url = request.getQueryString() == null ?
                reqUrl.toString() :
                reqUrl.append('?').append(request.getQueryString()).toString();

        long contentLength = request.getContentLengthLong();
        return TELEMETRY.getTracer().spanBuilder(operationName)
                .setParent(extractedContext)
                .setSpanKind(SpanKind.SERVER)
                .setAttribute(SemanticAttributes.HTTP_REQUEST_METHOD, operationName)
                .setAttribute(SemanticAttributes.HTTP_REQUEST_BODY_SIZE, contentLength)
                .setAttribute(SemanticAttributes.HTTP_ROUTE, url)
                .startSpan();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse
            response, Object handler, Exception ex) throws Exception {
        Object attr = request.getAttribute(HTTP_SPAN_SCOPE);
        if (attr == null) {
            return;
        }

        try (Scope scope = (Scope) attr) {
            Span current = Span.current();
            current.setAttribute(SemanticAttributes.HTTP_RESPONSE_STATUS_CODE, response.getStatus());
            if (ex != null) {
                current.setAttribute("span.server.error", ex.getClass().getName());
            }
            current.end();
        }

        ServiceMeter metric = (ServiceMeter) request.getAttribute(HTTP_SERVICE_METER);
        if (metric == null) {
            return;
        }

        if (ex != null) {
            metric.setError(ex);
        }
        metric.end();
    }
}
