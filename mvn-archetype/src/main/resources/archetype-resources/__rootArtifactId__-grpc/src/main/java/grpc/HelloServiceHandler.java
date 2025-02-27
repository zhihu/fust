#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.grpc;

import com.google.protobuf.Timestamp;
import ${package}.hello.HelloRequest;
import ${package}.hello.HelloResponse;
import ${package}.hello.HelloServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class HelloServiceHandler extends HelloServiceGrpc.HelloServiceImplBase {
    @Override
    public void hello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        // 处理请求
        String name = request.getName();
        String message = "Hello, " + name + "!";
        HelloResponse response = HelloResponse.newBuilder()
                .setNow(Timestamp.newBuilder().setSeconds(LocalDateTime.now().getSecond()).build())
                .setMessage(message).build();
        // 发送响应
        responseObserver.onNext(response);
        // 完成RPC调用
        responseObserver.onCompleted();
    }
}
