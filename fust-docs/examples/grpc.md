# gRPC 服务示例

本示例演示如何使用FUST框架开发gRPC服务和客户端，包括服务定义、服务实现、客户端调用以及与Spring Boot的集成。

## 示例概述

FUST提供了两种使用gRPC的方式：

1. **直接使用fust-armeria-grpc组件** - 适用于非Spring环境或需要完全控制gRPC服务配置的场景
2. **通过fust-boot-grpc集成Spring Boot** - 简化配置，利用Spring的依赖注入和自动配置能力

本示例将同时展示这两种方式的实现。

## 前置条件

- JDK 11+
- Maven 3.6+ 或 Gradle 7.0+
- Protobuf编译器（可选，如使用buf工具则不需要）

## 示例一：基础gRPC服务

### 1. 定义服务接口

首先，创建proto文件定义服务接口：

```protobuf
// hello.proto
syntax = "proto3";

package hello;

option java_package = "com.zhihu.fust.grpc.examples.proto";
option java_multiple_files = true;

// The greeting service definition.
service HelloWorld {
  // Sends a greeting
  rpc SayHello (HelloRequest) returns (HelloResponse) {}
}

// The request message containing the user's name.
message HelloRequest {
  string name = 1;
}

// The response message containing the greeting
message HelloResponse {
  string message = 1;
}
```

### 2. 实现服务端

创建gRPC服务实现类：

```java
public class HelloWorldImpl extends HelloWorldGrpc.HelloWorldImplBase {
    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        HelloResponse response = HelloResponse.newBuilder()
                .setMessage("Hello " + request.getName())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
```

创建服务器启动类：

```java
public class MainServer {
    private static final Logger log = LoggerFactory.getLogger(MainServer.class);

    public static void main(String[] args) {
        // 初始化环境
        Env.init();
        TelemetryInitializer.init();
        ILoggingSystem.get().initialize();
        
        // 创建服务器构建器
        int port = 8010;
        GrpcServerBuilder builder = GrpcServerBuilder.builder(port);
        
        // 添加请求监控
        builder.requestMonitor(reqLog -> {
            long totalTimeMs = TimeUnit.NANOSECONDS.toMillis(reqLog.totalDurationNanos());
            RequestHeaders headers = reqLog.requestHeaders();
            log.warn("monitor|headers={} totalTimeMs={}ms", headers, totalTimeMs);
        });
        
        // 添加服务实现并启动服务器
        builder.addService(new HelloWorldImpl())
                .build()
                .start()
                .join();
    }
}
```

### 3. 实现客户端

创建gRPC客户端调用服务：

```java
public class MainClient {
    private static final Logger log = LoggerFactory.getLogger(MainClient.class);

    public static void main(String[] args) {
        // 初始化环境
        Env.init();
        TelemetryInitializer.init();
        ILoggingSystem.get().initialize();
        
        // 创建客户端
        HelloWorldGrpc.HelloWorldStub helloService = GrpcClientBuilder
                .builder(HelloWorldGrpc.HelloWorldStub.class)
                .endpoint("localhost", 8010)
                .build();
        
        // 发送请求
        HelloRequest request = HelloRequest.newBuilder()
                .setName("World")
                .build();
        
        helloService.sayHello(request, new StreamObserver<HelloResponse>() {
            @Override
            public void onNext(HelloResponse response) {
                log.info("Response: {}", response.getMessage());
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("Error: ", throwable);
            }

            @Override
            public void onCompleted() {
                log.info("Completed");
            }
        });
        
        // 等待一段时间以确保请求完成
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

### 4. 运行示例

首先启动服务端：

```bash
java -cp <classpath> com.zhihu.fust.grpc.examples.server.MainServer
```

然后运行客户端：

```bash
java -cp <classpath> com.zhihu.fust.grpc.examples.client.MainClient
```

## 示例二：与Spring Boot集成

### 1. 定义服务接口

在`proto/hello/hello.proto`中定义服务，添加HTTP注解以支持REST API转换：

```protobuf
syntax = "proto3";

package hello;

import "google/api/annotations.proto";

option java_package = "com.zhihu.fust.example.grpc.proto";
option java_multiple_files = true;
option go_package = "hello/";

service HelloService {
  rpc Hello (HelloRequest) returns (HelloResponse) {
    option (google.api.http) = {
      post: "/v1/hello"
      body: "*"
    };
  }
}

message HelloRequest {
  string name = 1;
}

message HelloResponse {
  string message = 1;
}
```

### 2. 实现服务端

创建服务实现类，支持Spring依赖注入：

```java
@Component
public class HelloServiceHandler extends HelloServiceGrpc.HelloServiceImplBase {
    private final RedisService redisService;
    
    public HelloServiceHandler(RedisService redisService) {
        this.redisService = redisService;
    }
    
    @Override
    public void hello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        String name = request.getName();
        String time = redisService.get("time:" + name);
        if (time == null) {
            time = LocalDateTime.now().toString();
            redisService.set("time:" + name, time);
        }
        
        HelloResponse response = HelloResponse.newBuilder()
                .setMessage("Hello " + name + ", time: " + time)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
```

创建gRPC服务器组件：

```java
@Component
@RequiredArgsConstructor
public class GrpcServer {
    private final HelloServiceHandler helloServiceHandler;
    
    public void start() {
        GrpcServerBuilder builder = GrpcServerBuilder.builder(8888);
        builder.enableHttpJsonTranscoding(true)
                .addService(helloServiceHandler)
                .build()
                .start();
    }
}
```

创建Spring Boot应用主类：

```java
@SpringBootApplication
public class GrpcMain {
    private final GrpcServer grpcServer;
    
    public GrpcMain(GrpcServer grpcServer) {
        this.grpcServer = grpcServer;
    }
    
    public static void main(String[] args) {
        Telemetry.initialize();
        ConfigurableApplicationContext context = new SpringApplicationBuilder(GrpcMain.class)
                .run(args);
        GrpcMain main = context.getBean(GrpcMain.class);
        main.grpcServer.start();
    }
}
```

### 3. 配置Spring Boot应用

创建`application.yml`配置文件：

```yaml
spring:
  application:
    name: fust-boot-example-grpc
  
  # Redis配置
  redis:
    host: localhost
    port: 6379
    database: 0
```

### 4. 调用服务

#### 使用gRPC客户端调用

```java
public class GrpcClientExample {
    public static void main(String[] args) {
        HelloServiceGrpc.HelloServiceBlockingStub stub = GrpcClientBuilder
                .builder(HelloServiceGrpc.HelloServiceBlockingStub.class)
                .endpoint("localhost", 8888)
                .build();
        
        HelloRequest request = HelloRequest.newBuilder()
                .setName("Spring")
                .build();
        
        HelloResponse response = stub.hello(request);
        System.out.println("Response: " + response.getMessage());
    }
}
```

#### 使用HTTP/JSON调用

由于启用了HTTP/JSON转码，还可以使用HTTP客户端调用：

```bash
curl -X POST http://localhost:8888/v1/hello -d '{"name":"Spring"}'
```

### 5. 运行Spring Boot应用

使用Maven:

```bash
mvn spring-boot:run -pl fust-boot-example-grpc
```

或使用Gradle:

```bash
./gradlew :fust-boot-example-grpc:bootRun
```

## 高级特性示例

### 1. 双向流RPC

在proto文件中定义双向流服务：

```protobuf
service EchoService {
  rpc Echo (stream EchoRequest) returns (stream EchoResponse) {}
}

message EchoRequest {
  string message = 1;
}

message EchoResponse {
  string message = 1;
}
```

实现服务端：

```java
public class EchoServiceImpl extends EchoServiceGrpc.EchoServiceImplBase {
    @Override
    public StreamObserver<EchoRequest> echo(StreamObserver<EchoResponse> responseObserver) {
        return new StreamObserver<EchoRequest>() {
            @Override
            public void onNext(EchoRequest request) {
                EchoResponse response = EchoResponse.newBuilder()
                        .setMessage("Echo: " + request.getMessage())
                        .build();
                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
```

客户端调用双向流接口：

```java
public void callEchoService() {
    EchoServiceGrpc.EchoServiceStub stub = GrpcClientBuilder
            .builder(EchoServiceGrpc.EchoServiceStub.class)
            .endpoint("localhost", 8010)
            .build();
    
    StreamObserver<EchoRequest> requestObserver = stub.echo(new StreamObserver<EchoResponse>() {
        @Override
        public void onNext(EchoResponse response) {
            System.out.println("Received: " + response.getMessage());
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
        }

        @Override
        public void onCompleted() {
            System.out.println("Stream completed");
        }
    });
    
    try {
        // 发送多个消息
        for (int i = 0; i < 5; i++) {
            requestObserver.onNext(EchoRequest.newBuilder()
                    .setMessage("Message " + i)
                    .build());
            
            Thread.sleep(100);
        }
        
        // 标记客户端流完成
        requestObserver.onCompleted();
        
        // 等待服务器响应
        Thread.sleep(1000);
    } catch (InterruptedException e) {
        requestObserver.onError(e);
        Thread.currentThread().interrupt();
    }
}
```

### 2. 自定义拦截器

创建请求日志拦截器：

```java
public class LoggingInterceptor implements ServerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);
    
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, 
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        String methodName = call.getMethodDescriptor().getFullMethodName();
        log.info("Received call to {}", methodName);
        
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
                next.startCall(call, headers)) {
            @Override
            public void onMessage(ReqT message) {
                log.info("Request message: {}", message);
                super.onMessage(message);
            }
        };
    }
}
```

通过SPI注册拦截器：

1. 创建拦截器工厂：

```java
public class CustomInterceptorFactory implements GrpcServerInterceptorFactory {
    @Override
    public ServerInterceptor create() {
        return new LoggingInterceptor();
    }
}
```

2. 在`META-INF/services/com.zhihu.fust.armeria.grpc.server.GrpcServerInterceptorFactory`文件中添加：

```
com.example.CustomInterceptorFactory
```

### 3. 设置请求超时

客户端设置请求超时：

```java
HelloWorldGrpc.HelloWorldFutureStub stub = GrpcClientBuilder
        .builder(HelloWorldGrpc.HelloWorldFutureStub.class)
        .endpoint("localhost", 8010)
        .build()
        .withDeadlineAfter(5, TimeUnit.SECONDS);

HelloRequest request = HelloRequest.newBuilder()
        .setName("World")
        .build();

try {
    HelloResponse response = stub.sayHello(request).get();
    System.out.println("Response: " + response.getMessage());
} catch (ExecutionException e) {
    if (e.getCause() instanceof StatusRuntimeException) {
        StatusRuntimeException statusException = (StatusRuntimeException) e.getCause();
        if (statusException.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
            System.out.println("Request timed out");
        }
    }
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}
```

## 示例源码

完整的示例代码可以在以下位置找到：

- **基础gRPC服务示例**：`examples/fust-components-example/grpc`
- **Spring Boot集成示例**：`examples/fust-boot-example/fust-boot-example-grpc`

## 常见问题

### 1. 服务启动失败

**问题**：服务启动时报端口已被占用错误。

**解决方案**：确保指定的端口未被其他进程使用，或者修改服务使用的端口号。

### 2. 客户端连接失败

**问题**：客户端无法连接到服务器。

**解决方案**：
- 确保服务器已启动
- 检查主机和端口配置是否正确
- 检查网络连接和防火墙设置

### 3. 消息大小错误

**问题**：发送或接收大型消息时出现错误。

**解决方案**：增加最大请求或响应消息大小：

```java
// 服务端设置
builder.maxRequestMessageLength(10 * 1024 * 1024);

// 客户端设置
GrpcClientBuilder.builder(...)
    .maxInboundMessageSize(10 * 1024 * 1024)
    .build();
```

## 总结

FUST gRPC组件提供了灵活、高性能的gRPC服务开发体验，支持多种模式，从简单的独立服务到与Spring Boot集成的微服务应用。通过本示例，您应该能够：

1. 创建并定义gRPC服务接口
2. 实现服务端逻辑
3. 构建客户端调用服务
4. 将gRPC服务与Spring Boot集成
5. 使用高级功能如双向流、拦截器和请求超时设置

更多详细信息，请参阅[gRPC组件文档](/components/grpc.md)。
