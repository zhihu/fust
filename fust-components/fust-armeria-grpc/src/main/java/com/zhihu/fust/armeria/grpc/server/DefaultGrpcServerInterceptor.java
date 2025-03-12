package com.zhihu.fust.armeria.grpc.server;

import com.zhihu.fust.armeria.grpc.GrpcTelemetry;
import com.zhihu.fust.armeria.grpc.exception.GrpcBusinessError;
import com.zhihu.fust.armeria.grpc.exception.GrpcErrorInfo;
import com.zhihu.fust.commons.exception.ExceptionUtils;
import com.zhihu.fust.telemetry.api.ServiceEntry;
import com.zhihu.fust.telemetry.api.ServiceMeter;
import com.zhihu.fust.telemetry.api.ServiceMeterKind;
import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.*;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * grpc server interceptor
 */
public class DefaultGrpcServerInterceptor implements ServerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(DefaultGrpcServerInterceptor.class);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
                                                                 Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> next) {

        Context parent = GrpcTelemetry.extract(headers);
        String methodName = GrpcTelemetry.getMethodName(call.getMethodDescriptor());
        Span span = GrpcTelemetry.getTracer()
                .spanBuilder(methodName)
                .setParent(parent)
                .setSpanKind(SpanKind.SERVER)
                .startSpan();

        Context context = Context.current().with(span)
                .with(ServiceEntry.SERVICE_ENTRY_KEY, ServiceEntry.create(methodName));

        Map<String, Object> customContextMap = new HashMap<>(8);
        customContextMap.put(GrpcServerContext.KEY_HEADERS, headers);

        ServiceMeter serviceMeter = GrpcTelemetry.createServiceMeter(ServiceMeterKind.SERVER);
        serviceMeter.setMethod(methodName);

        SimpleForwardingServerCall<ReqT, RespT> forwardingCall = new SimpleForwardingServerCall<ReqT, RespT>(
                call) {
            @Override
            public void close(Status status, Metadata trailers) {
                Throwable cause = status.getCause();
                if (!status.isOk()) {
                    GrpcErrorInfo bizError = extractBizError(cause);
                    injectBusinessErrorToMetadata(bizError, trailers);
                    String errorCode = status.getCode().name();
                    if (bizError != null) {
                        errorCode = bizError.getCode();
                    } else if (cause != null) {
                        errorCode = ExceptionUtils.getRootCause(cause).getClass().getSimpleName();
                    }

                    boolean shouldRecord = bizError != null || GrpcTelemetry.enableGrpcBizErrorMeter();
                    if (shouldRecord) {
                        GrpcTelemetry.addExceptionEvent(span, errorCode);
                        serviceMeter.setError(errorCode);
                    }
                }
                super.close(status, trailers);
            }
        };

        ServerCall.Listener<ReqT> listener = next.startCall(forwardingCall, headers);
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {

            Scope scope;

            @Override
            public void onHalfClose() {
                scope = context.makeCurrent();
                GrpcServerContext.set(customContextMap);
                try {
                    super.onHalfClose();  // 调用 io.grpc.stub.ServerCalls.UnaryMethod#invoke
                } catch (RuntimeException e) {
                    // Handling those exceptions that not set to response.onError() method
                    GrpcErrorInfo bizError = extractBizError(e);
                    if (bizError != null) {
                        // exception is a business error
                        String errorCode = bizError.getCode();
                        if (GrpcTelemetry.enableGrpcBizErrorMeter()) {
                            GrpcTelemetry.addExceptionEvent(span, errorCode);
                            serviceMeter.setError(errorCode);
                        }
                        Metadata errorMeta = new Metadata();
                        injectBusinessErrorToMetadata(bizError, errorMeta);
                        throw new StatusRuntimeException(Status.UNKNOWN.withCause(e), errorMeta);
                    } else {
                        // exception is an unknown error, use exception class name
                        String errorName = ExceptionUtils.getRootCause(e).getClass().getSimpleName();
                        GrpcTelemetry.addExceptionEvent(span, errorName);
                        serviceMeter.setError(errorName);
                        throw e;
                    }
                }
            }

            @Override
            public void onCancel() {
                super.onCancel();
                end();
            }

            @Override
            public void onComplete() {
                super.onComplete();
                end();
            }

            private void end() {
                GrpcServerContext.remove();
                if (scope != null) {
                    scope.close();
                }
                GrpcTelemetry.endSpan(span);
                serviceMeter.end();
            }

        };
    }

    @Nullable
    private static GrpcErrorInfo extractBizError(Throwable e) {
        if (e == null) {
            return null;
        }
        if (GrpcBusinessError.class.isAssignableFrom(e.getClass())) {
            GrpcBusinessError grpcBusinessException = (GrpcBusinessError) e;
            return new GrpcErrorInfo(grpcBusinessException.getCode(), grpcBusinessException.getDescription());
        }
        return null;
    }

    private static void injectBusinessErrorToMetadata(GrpcErrorInfo errorInfo, Metadata metadata) {
        if (errorInfo != null) {
            String value = GrpcErrorInfo.toJson(errorInfo);
            if (value != null) {
                metadata.put(GrpcErrorInfo.GRPC_BIZ_ERROR_BIN_KEY, value.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}
