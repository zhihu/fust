package com.zhihu.fust.example.grpc;

import com.google.protobuf.Timestamp;
import com.zhihu.fust.example.business.service.RedisService;
import com.zhihu.fust.example.hello.HelloRequest;
import com.zhihu.fust.example.hello.HelloResponse;
import com.zhihu.fust.example.hello.HelloServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Log4j2
public class HelloServiceHandler extends HelloServiceGrpc.HelloServiceImplBase {
    private final RedisService redisService;

    @Override
    public void hello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        // 处理请求
        String name = request.getName();
        String message = "Hello, " + name + "!";
        String time = redisService.getHelloTime(name);
        if (time == null) {
            time = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + "";
            redisService.cacheHello(name, time);
        }
        long seconds = Long.parseLong(time);

        HelloResponse response = HelloResponse.newBuilder()
                .setNow(Timestamp.newBuilder().setSeconds(seconds).build())
                .setMessage(message).build();
        log.info("response: {}", response);
        // 发送响应
        responseObserver.onNext(response);
        // 完成RPC调用
        responseObserver.onCompleted();
    }
}
