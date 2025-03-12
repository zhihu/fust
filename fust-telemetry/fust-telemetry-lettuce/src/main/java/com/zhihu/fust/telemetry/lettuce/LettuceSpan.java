package com.zhihu.fust.telemetry.lettuce;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.zhihu.fust.telemetry.api.ServiceMeterKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhihu.fust.telemetry.api.ServiceMeter;
import com.zhihu.fust.telemetry.api.ServiceEntry;

import io.lettuce.core.protocol.CompleteableCommand;
import io.lettuce.core.protocol.RedisCommand;
import io.lettuce.core.tracing.Tracer;
import io.lettuce.core.tracing.Tracing;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.semconv.SemanticAttributes;

/**
 * span for lettuce
 *
 * @author yanzhuzhu
 * @date 2020-01-15
 */
public class LettuceSpan extends Tracer.Span {

    /**
     * lettuce key args in lettuce
     */
    private static final String REDIS_ARGS = "lettuce.args";

    /**
     * lettuce key
     */
    public static final String REDIS_KEY = "lettuce.key";
    private final LettuceTraceContext context;
    private final io.opentelemetry.api.trace.Tracer tracer;
    private final Map<String, String> tagValues = new HashMap<>();
    private String commandName;
    private LettuceTracing.LettuceEndpoint endpoint;
    private Span span;
    private ServiceMeter serviceMeter;

    public static final Logger log = LoggerFactory.getLogger(LettuceSpan.class);

    private boolean includeCommandArgs;

    public LettuceSpan(io.opentelemetry.api.trace.Tracer tracer, LettuceTraceContext context, boolean includeCommandArgs) {
        this.tracer = tracer;
        this.context = context;
        this.includeCommandArgs = includeCommandArgs;
    }

    @Override
    public Tracer.Span name(String name) {
        commandName = name;
        return this;
    }

    @Override
    public Tracer.Span remoteEndpoint(Tracing.Endpoint endpoint) {
        this.endpoint = (LettuceTracing.LettuceEndpoint) endpoint;
        return this;
    }


    @Override
    public Tracer.Span start(RedisCommand<?, ?, ?> cmd) {
        String spanName = endpoint.getServiceName() + '_' + commandName;

        SpanBuilder spanBuilder = tracer.spanBuilder(spanName)
                .setSpanKind(SpanKind.CLIENT)
                .setAttribute(SemanticAttributes.PEER_SERVICE, endpoint.getServiceName())
                .setAttribute(SemanticAttributes.DB_SYSTEM, "Redis")
                .setAttribute(SemanticAttributes.DB_OPERATION, commandName)
                .setAttribute(SemanticAttributes.DB_CONNECTION_STRING, endpoint.getServiceAddress());

        // set parent from context
        Optional.ofNullable(context)
                .map(LettuceTraceContext::getParent)
                .ifPresent(spanBuilder::setParent);

        if (!tagValues.isEmpty()) {
            tagValues.forEach(spanBuilder::setAttribute);
            tagValues.clear();
        }

        // start span
        span = spanBuilder.startSpan();

        // set metric
        String entryName = Optional.ofNullable(context)
                .map(LettuceTraceContext::getServiceEntry)
                .map(ServiceEntry::getEntry)
                .orElse("internal");

        serviceMeter = LettuceTelemetry.getTelemetry().createServiceMeter(ServiceMeterKind.CLIENT);
        serviceMeter.setMethod(entryName);
        serviceMeter.setTargetMethod(commandName);
        serviceMeter.setTargetService(endpoint.getServiceName());

        if (includeCommandArgs && cmd.getArgs() != null) {
            span.setAttribute(REDIS_ARGS, cmd.getArgs().toCommandString());
        }

        // lettuce 6 需要监听命令结束来处理指标的上报，参考 BraveSpan 的实现
        if (cmd instanceof CompleteableCommand) {
            CompleteableCommand<?> completeableCommand = (CompleteableCommand<?>) cmd;
            completeableCommand.onComplete((o, throwable) -> {
                if (cmd.getOutput() != null) {
                    if (throwable != null) {
                        serviceMeter.setError(throwable);
                        span.setAttribute("error", serviceMeter.getError());
                    }
                }

                finish();
            });
        } else {
            throw new IllegalArgumentException("Command " + cmd
                    + " must implement CompleteableCommand to attach Span completion to command completion");
        }

        return this;
    }

    /**
     * after span start
     */
    @Override
    public Tracer.Span annotate(String value) {
        if (span != null) {
            span.addEvent(value);
        }
        return this;
    }

    @Override
    public Tracer.Span tag(String key, String value) {
        // lettuce key tag
        if (REDIS_ARGS.equals(key)) {
            String redisKey = findRedisKey(value);
            return doTag(REDIS_KEY, redisKey);
        }
        return doTag(key, value);
    }

    private Tracer.Span doTag(String key, String value) {
        if (span == null) {
            tagValues.put(key, value);
        } else {
            span.setAttribute(key, value);
        }
        return this;
    }

    /**
     * args fmt like key<my_key> 60, see KeyArgument in lettuce
     */
    private static String findRedisKey(String args) {
        int start = args.indexOf('<');
        int end = args.indexOf('>');
        if (start == -1 || end == -1) {
            return "";
        }
        return args.substring(start + 1, end);
    }

    @Override
    public Tracer.Span error(Throwable throwable) {
        serviceMeter.setError(throwable);
        span.setAttribute("error", serviceMeter.getError());
        return this;
    }


    @Override
    public void finish() {
        if (span != null) {
            try {
                span.end();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        if (serviceMeter != null) {
            serviceMeter.end();
        }

    }

}
