package com.zhihu.fust.grpc.examples.server;

import examples.api.echo.EchoServiceGrpc;
import examples.api.echo.Request;
import examples.api.echo.Response;
import io.grpc.stub.StreamObserver;

public class EchoServiceImpl extends EchoServiceGrpc.EchoServiceImplBase {
    @Override
    public void echo(Request request, StreamObserver<Response> responseObserver) {
        Response resp = Response.newBuilder()
                .setMessage(request.getMessage())
                .build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }
}
