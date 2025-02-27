package com.zhihu.fust.grpc.examples.server;

import com.linecorp.armeria.server.ServiceRequestContext;
import com.zhihu.fust.grpc.examples.BusinessException;
import examples.api.hello.HelloRequest;
import examples.api.hello.HelloResponse;
import examples.api.hello.HelloServiceGrpc;
import io.grpc.stub.StreamObserver;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * greeter service
 */
public class HelloWorldImpl extends HelloServiceGrpc.HelloServiceImplBase {
    private static final AtomicLong counter = new AtomicLong();
    private static final Logger log = LoggerFactory.getLogger(HelloWorldImpl.class);

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        log.info("process req|{}", request.getFullName());
        // 获取头信息
        ServiceRequestContext context = ServiceRequestContext.current();
        Object time = context.attr(AttributeKey.valueOf("requestTotalTime"));
        context.addAdditionalResponseHeader("X-ZONE-SERVER-REGION", "local-server");

        Base64.getEncoder().encodeToString(request.toByteArray());

        String name = request.getName();
        String fullName = request.getFullName();
        HelloResponse resp = HelloResponse.newBuilder()
                .setChkMsg("FullName:" + fullName)
                .setReply("Name: " + name).build();
        if (Objects.equals(name, "error")) {
            // 非预期内的异常
            responseObserver.onError(new IllegalAccessException("error"));
        } else if (Objects.equals(name, "biz_error")) {
            // 预期内的业务异常，方式二
            throw new BusinessException("error1", "msg1");
        } else if (Objects.equals(name, "biz_error2")) {
            // 预期内的业务异常，方式二
            responseObserver.onError(new BusinessException("error2", "msg2"));
        } else {
            responseObserver.onNext(resp);
            responseObserver.onCompleted();
        }


    }
}
