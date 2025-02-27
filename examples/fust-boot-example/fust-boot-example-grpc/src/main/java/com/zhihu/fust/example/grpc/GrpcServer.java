package com.zhihu.fust.example.grpc;

import com.zhihu.fust.armeria.grpc.server.GrpcServerBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GrpcServer {


    private final HelloServiceHandler helloServiceHandler;

    /**
     * 启动grpc服务
     */
    public void start() {
        GrpcServerBuilder builder = GrpcServerBuilder.builder(8888);
        builder.enableHttpJsonTranscoding(true)
                .addService(helloServiceHandler)
                .build()
                .start();
    }
}
