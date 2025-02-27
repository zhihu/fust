package com.zhihu.fust.armeria.grpc.server;

import com.linecorp.armeria.common.SerializationFormat;
import com.linecorp.armeria.common.grpc.GrpcSerializationFormats;
import com.linecorp.armeria.common.logging.RequestLog;
import com.zhihu.fust.armeria.commons.ArmeriaServerRequestLogDecorator;
import com.zhihu.fust.armeria.grpc.GrpcTelemetry;
import io.grpc.StatusRuntimeException;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Armeria Grpc 服务端指标统计
 * <a href="https://armeria.dev/docs/advanced-structured-logging/"></a>
 */
public class ArmeriaGrpcServerRequestLogDecorator extends ArmeriaServerRequestLogDecorator {

    public ArmeriaGrpcServerRequestLogDecorator(@Nullable Consumer<RequestLog> requestMonitor) {
        super(requestMonitor);
    }

    @Override
    protected String getMethodName(RequestLog requestLog) {
        return GrpcTelemetry.getMethodByRequestLog(requestLog);
    }

    @Override
    protected boolean isRpcRequest(SerializationFormat format) {
        return GrpcSerializationFormats.isGrpc(format);
    }

    @Override
    protected String getError(Throwable responseCause) {
        String error = responseCause.getClass().getSimpleName();
        if (responseCause instanceof StatusRuntimeException) {
            StatusRuntimeException statusRuntimeException = (StatusRuntimeException) responseCause;
            error = statusRuntimeException.getStatus().getCode().name();
        }
        return error;
    }
}
