package com.zhihu.fust.armeria.commons;

import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.SerializationFormat;
import com.linecorp.armeria.common.logging.RequestLog;
import com.linecorp.armeria.common.logging.RequestLogProperty;
import com.linecorp.armeria.internal.shaded.guava.collect.Lists;
import com.linecorp.armeria.server.DecoratingHttpServiceFunction;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.zhihu.fust.core.env.Env;
import com.zhihu.fust.telemetry.api.MeterClient;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Armeria request log decorator
 * <a href="https://armeria.dev/docs/advanced-structured-logging/"></a>
 */
public abstract class ArmeriaServerRequestLogDecorator implements DecoratingHttpServiceFunction {

    private static final Logger log = LoggerFactory.getLogger(ArmeriaServerRequestLogDecorator.class);

    private static final String UNKNOWN = "unknown";
    private static final String REQUEST_COUNT = "armeria.server.rpc.request.count";
    private static final String REQUEST_DURATION_TIME = "armeria.server.rpc.request.time";
    private static final String RESPONSE_DURATION_TIME = "armeria.server.rpc.response.time";
    private static final String TOTAL_TIME = "armeria.server.rpc.total.time";
    private static final String RESPONSE_ERROR = "armeria.server.rpc.response.error.count";
    protected static final MeterClient METER_CLIENT = MeterClient.getMeterClient();

    @Nullable
    private final Consumer<RequestLog> requestMonitor;

    public ArmeriaServerRequestLogDecorator(@Nullable Consumer<RequestLog> requestMonitor) {
        this.requestMonitor = requestMonitor;
    }

    @Override
    public HttpResponse serve(HttpService delegate, ServiceRequestContext ctx, HttpRequest req)
            throws Exception {
        ctx.log().whenAvailable(RequestLogProperty.RESPONSE_END_TIME)
                .whenComplete((requestLog, throwable) -> {
                    if (!isRpcRequest(requestLog.serializationFormat())) {
                        return;
                    }
                    recordMeter(requestLog);
                    if (requestMonitor != null) {
                        Context context = ArmeriaTelemetry.extract(Context.current(), requestLog.requestHeaders());
                        try (Scope ignored = context.makeCurrent()) {
                            requestMonitor.accept(requestLog);
                        } catch (Exception e) {
                            log.warn("serverMonitor error", e);
                        }
                    }
                });

        return delegate.serve(ctx, req);
    }

    protected abstract String getMethodName(RequestLog requestLog);

    protected abstract String getError(Throwable responseCause);

    protected abstract boolean isRpcRequest(SerializationFormat format);

    private void recordMeter(RequestLog requestLog) {
        long reqTime = NANOSECONDS.toMillis(requestLog.requestDurationNanos());
        long respTime = NANOSECONDS.toMillis(requestLog.responseDurationNanos());
        long totalTime = NANOSECONDS.toMillis(requestLog.totalDurationNanos());

        String appName = Optional.ofNullable(Env.getAppName()).orElse(UNKNOWN);
        String serviceName = Optional.ofNullable(Env.getServiceName()).orElse(UNKNOWN);
        String methodName = getMethodName(requestLog);
        String[] tags = {"app:" + appName, "service:" + serviceName, "method:" + methodName};

        METER_CLIENT.increment(REQUEST_COUNT, tags);
        METER_CLIENT.recordExecutionTime(REQUEST_DURATION_TIME, reqTime, tags);
        METER_CLIENT.recordExecutionTime(RESPONSE_DURATION_TIME, respTime, tags);
        METER_CLIENT.recordExecutionTime(TOTAL_TIME, totalTime, tags);

        Throwable responseCause = requestLog.responseCause();
        if (responseCause != null) {
            String error = getError(responseCause);
            ArrayList<String> tagList = Lists.newArrayList(tags);
            tagList.add("error:" + error);
            tags = tagList.toArray(new String[0]);
            METER_CLIENT.incrementCounter(RESPONSE_ERROR, tags);
        }
    }
}
