package org.example.simple.grpc;

import org.example.simple.user.proto.*;
import com.zhihu.fust.armeria.grpc.client.GrpcClientBuilder;

import java.util.Objects;

public class GrpcClient {
    UserServiceGrpc.UserServiceBlockingStub userServiceGrpc = GrpcClientBuilder
            .builder(UserServiceGrpc.UserServiceBlockingStub.class)
            .endpoint("127.0.0.1", 9090)
            .build();


    public User createUser(String name, String birthday) {
        return userServiceGrpc.createUser(CreateUserRequest.newBuilder()
                .setName("李三")
                .setBirthday("1991-02-21")
                .build());
    }

    public User queryUser(Long id) {
        Objects.requireNonNull(id);
        return userServiceGrpc.getUser(GetUserRequest.newBuilder()
                .setId(id)
                .build());
    }
}
