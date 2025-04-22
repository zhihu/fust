package org.example.simple.grpc;

import com.zhihu.fust.armeria.grpc.server.GrpcServerBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GrpcServer {

    private final UserHandler userHandler;

    /**
     * 启动grpc服务
     */
    public void start() {
        GrpcServerBuilder.builder(9090)
                .enableHttpJsonTranscoding(true)
                .addService(userHandler)
                .build()
                .start();
    }
}
