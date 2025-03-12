package com.zhihu.fust.armeria.grpc.client;

import com.linecorp.armeria.common.SerializationFormat;
import com.linecorp.armeria.common.grpc.GrpcSerializationFormats;
import com.linecorp.armeria.common.logging.RequestLog;
import com.zhihu.fust.armeria.commons.ArmeriaClientRequestLogDecorator;
import com.zhihu.fust.armeria.grpc.GrpcTelemetry;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ArmeriaGrpcClientRequestLogDecorator extends ArmeriaClientRequestLogDecorator {

    public ArmeriaGrpcClientRequestLogDecorator(@Nullable Consumer<RequestLog> requestMonitor) {
        super(requestMonitor);
    }

    @Override
    protected boolean isRpcRequest(SerializationFormat format) {
        return GrpcSerializationFormats.isGrpc(format);
    }

    @Override
    protected String getError(Throwable responseCause) {
        return responseCause.getClass().getSimpleName();
    }

    @Override
    protected String getMethodName(RequestLog requestLog) {
        return GrpcTelemetry.getMethodByRequestLog(requestLog);
    }

}
