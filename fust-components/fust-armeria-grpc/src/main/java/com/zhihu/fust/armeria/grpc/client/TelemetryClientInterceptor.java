package com.zhihu.fust.armeria.grpc.client;

import com.zhihu.fust.armeria.grpc.GrpcTelemetry;
import com.zhihu.fust.armeria.grpc.exception.GrpcBusinessError;
import com.zhihu.fust.armeria.grpc.exception.GrpcBusinessErrorExtractor;
import com.zhihu.fust.commons.exception.ExceptionUtils;
import com.zhihu.fust.telemetry.api.ServiceMeter;
import com.zhihu.fust.telemetry.api.ServiceMeterKind;
import io.grpc.*;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.semconv.SemanticAttributes;

import java.util.Optional;


public class TelemetryClientInterceptor implements ClientInterceptor {
    final String targetService;

    public TelemetryClientInterceptor(String targetService) {
        this.targetService = targetService;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new TelemetryClientCall<>(method, next.newCall(method, callOptions), next);
    }

    private final class TelemetryClientCall<ReqT, RespT> extends SimpleForwardingClientCall<ReqT, RespT> {

        private ManagedChannel channel;
        private Span span;
        private ServiceMeter serviceMeter;

        // not private to avoid synthetic class
        TelemetryClientCall(MethodDescriptor<ReqT, RespT> method, ClientCall<ReqT, RespT> call, Channel channel) {
            super(call);
            if (channel instanceof ManagedChannel) {
                this.channel = (ManagedChannel) channel;
            }
            if (targetService != null) {
                initTelemetry(method);
            }
        }

        @Override
        public void start(Listener<RespT> responseListener, Metadata headers) {
            injectHeaders(headers);
            Listener<RespT> injectListener = targetService != null ?
                    new TelemetryClientCallListener<>(responseListener, span, serviceMeter, channel)
                    : responseListener;
            super.start(injectListener, headers);
        }

        private void injectHeaders(Metadata headers) {
            GrpcTelemetry.inject(headers);
        }

        private void initTelemetry(MethodDescriptor<ReqT, RespT> method) {
            Tracer tracer = GrpcTelemetry.getTracer();
            String targetMethod = GrpcTelemetry.getMethodName(method);
            span = tracer.spanBuilder(targetMethod)
                    .setAttribute(SemanticAttributes.PEER_SERVICE, targetService)
                    .setSpanKind(SpanKind.CLIENT)
                    .startSpan();

            // 指标信息
            serviceMeter = GrpcTelemetry.createServiceMeter(ServiceMeterKind.CLIENT);
            String entryMethod = GrpcTelemetry.getServiceEntry().getEntry();
            serviceMeter.setMethod(entryMethod);
            serviceMeter.setTargetMethod(targetMethod);
            serviceMeter.setTargetService(targetService);
        }

    }

    private static final class TelemetryClientCallListener<RespT> extends SimpleForwardingClientCallListener<RespT> {

        private final Span span;
        private final ServiceMeter metric;
        private final ManagedChannel channel;

        TelemetryClientCallListener(ClientCall.Listener<RespT> responseListener, Span span, ServiceMeter metric, ManagedChannel channel) {
            super(responseListener);
            this.span = span;
            this.metric = metric;
            this.channel = channel;
        }

        @Override
        public void onHeaders(Metadata headers) {
        }

        @Override
        public void onClose(Status status, Metadata trailers) {
            if (!status.isOk()) {
                if (channel != null && status.getCode() == Status.Code.DEADLINE_EXCEEDED &&
                        ConnectivityState.READY != channel.getState(false)) {
                    status = Status.UNAVAILABLE.withDescription("Not connected");
                }
                Optional<GrpcBusinessError> grpcBizError = Optional.ofNullable(GrpcBusinessErrorExtractor.fromTrailers(trailers));
                if (grpcBizError.isPresent()) {
                    // biz error, record if enabled
                    if (GrpcTelemetry.enableGrpcBizErrorMeter()) {
                        metric.setError(grpcBizError.get().getCode());
                    }
                } else if (status.getCause() != null) {
                    // other exceptions, use the name of the exception class for easy identification
                    String errorName = ExceptionUtils.getRootCause(status.getCause())
                            .getClass().getSimpleName();
                    metric.setError(errorName);
                } else {
                    // use code for other cases
                    metric.setError(status.getCode().name());
                }
            }
            super.onClose(status, trailers);
            GrpcTelemetry.endSpan(span);
            metric.end();
        }
    }


}
