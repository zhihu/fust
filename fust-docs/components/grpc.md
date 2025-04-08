# gRPC 服务

FUST gRPC组件基于Armeria构建，提供高性能的gRPC服务器和客户端实现，支持HTTP/JSON转码、健康检查、请求/响应监控、拦截器扩展和分布式追踪等特性。

## 功能特性

- **高性能**: 基于Armeria构建，提供高吞吐量和低延迟的gRPC服务
- **HTTP/JSON转码**: 自动将gRPC服务转换为RESTful API，无需额外编码
- **健康检查服务**: 内置健康检查端点，方便服务监控
- **请求监控**: 支持请求完成和客户端超时的监控
- **拦截器扩展**: 通过SPI机制支持自定义服务端和客户端拦截器
- **分布式追踪**: 与FUST可观测性组件无缝集成，提供分布式追踪能力
- **文档服务**: 在开发和测试环境自动提供API文档服务
- **线程池管理**: 支持自定义阻塞任务执行器配置

## 使用buf生成协议代码

FUST推荐使用[buf](https://buf.build)工具生成gRPC协议代码，buf是一个现代化的Protobuf工具链，提供了更好的依赖管理和代码生成体验。

### 安装buf

在macOS上，可以使用Homebrew安装buf：

```bash
brew install bufbuild/buf/buf
```

对于其他操作系统，请参考[buf官方安装指南](https://buf.build/docs/installation)。

### 配置buf

在FUST项目中使用buf通常需要两个配置文件：

1. **buf.yaml** - 定义模块配置和依赖
2. **buf.gen.yaml** - 定义代码生成配置

#### 示例 `proto/buf.yaml`

```yaml
version: v1
deps:
  - buf.build/googleapis/googleapis  # 引入Google APIs以支持HTTP注解
  - buf.build/grpc-ecosystem/grpc-gateway  # 用于HTTP/JSON转码
lint:
  use:
    - DEFAULT
  except:
    - PACKAGE_DIRECTORY_MATCH
    - PACKAGE_VERSION_SUFFIX
breaking:
  use:
    - FILE
```

#### 示例 `buf.gen.yaml`

```yaml
version: v1
plugins:
  # 生成Java Protobuf代码
  - plugin: buf.build/protocolbuffers/java:v25.2
    out: ./gen-src/protobuf/java
  # 生成Java gRPC代码
  - plugin: buf.build/grpc/java:v1.61.0
    out: ./gen-src/protobuf/java
  # 可选：生成Go Protobuf代码
  - plugin: buf.build/protocolbuffers/go
    out: ./gen-src/gen-go
  # 可选：生成Go gRPC代码
  - plugin: buf.build/grpc/go:v1.3.0
    out: ./gen-src/gen-go
  # 可选：生成gRPC Gateway代码（用于RESTful转换）
  - plugin: buf.build/grpc-ecosystem/gateway:v2.19.0
    out: ./gen-src/gen-go-gateway
```

### 目录结构

推荐的目录结构如下：

```
your-project/
├── proto/
│   ├── buf.yaml            # buf模块配置
│   ├── buf.lock            # 依赖锁定文件（自动生成）
│   └── your-service/       # 您的服务定义目录
│       └── service.proto   # Proto文件
├── buf.gen.yaml            # 代码生成配置
└── gen-src/                # 生成的代码目录
    └── protobuf/
        └── java/           # 生成的Java代码
```

### 生成代码

有两种方式使用buf生成代码：

#### 方式一：直接指定配置文件

在项目根目录执行：

```bash
buf generate --template buf.gen.yaml
```

这种方式适用于简单项目，生成的代码会根据`buf.gen.yaml`中的配置输出到指定目录。

#### 方式二：指定proto目录

对于更复杂的项目结构，可以在项目根目录执行：

```bash
# 首先更新依赖
cd proto
buf mod update
cd ..

# 生成代码
buf generate proto
```

这种方式会使用`proto`目录中的`buf.yaml`配置，并根据项目根目录的`buf.gen.yaml`生成代码。

### 与Maven/Gradle集成

对于Maven项目，可以使用`build-helper-maven-plugin`将生成的代码目录添加为源目录：

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>build-helper-maven-plugin</artifactId>
    <version>3.2.0</version>
    <executions>
        <execution>
            <id>add-source</id>
            <phase>generate-sources</phase>
            <goals>
                <goal>add-source</goal>
            </goals>
            <configuration>
                <sources>
                    <source>gen-src/protobuf/java</source>
                </sources>
            </configuration>
        </execution>
    </executions>
</plugin>
```

对于Gradle项目，可以在`build.gradle`中添加：

```groovy
sourceSets {
    main {
        java {
            srcDirs += ['gen-src/protobuf/java']
        }
    }
}
```

### HTTP/JSON转码配置

要启用HTTP/JSON转码功能，需要在proto文件中添加HTTP注解，例如：

```protobuf
syntax = "proto3";

package hello;

import "google/api/annotations.proto";

option java_package = "com.zhihu.fust.example.grpc.proto";
option java_multiple_files = true;

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

## 快速开始

### 添加依赖

```xml
<dependency>
    <groupId>com.zhihu.fust</groupId>
    <artifactId>fust-armeria-grpc</artifactId>
</dependency>
```

### 服务端实现

创建一个简单的gRPC服务器：

```java
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

// 添加服务实现
builder.addService(new HelloWorldImpl())
      .addService(new EchoServiceImpl())
      .build()
      .start()
      .join();
```

### 客户端实现

创建并使用gRPC客户端：

```java
// 创建客户端
HelloServiceGrpc.HelloServiceStub helloService = GrpcClientBuilder
        .builder(HelloServiceGrpc.HelloServiceStub.class)
        .endpoint("localhost", 8010)
        .build();

// 发送请求
HelloRequest request = HelloRequest.newBuilder()
        .setName("World")
        .build();
helloService.hello(request, new StreamObserver<HelloResponse>() {
    @Override
    public void onNext(HelloResponse response) {
        System.out.println("Response: " + response.getMessage());
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onCompleted() {
        System.out.println("Completed");
    }
});
```

## Spring Boot 集成

FUST提供了与Spring Boot的无缝集成，简化gRPC服务的开发。

### 添加依赖

```xml
<dependency>
    <groupId>com.zhihu.fust</groupId>
    <artifactId>fust-boot-grpc</artifactId>
</dependency>
```

### 创建服务实现

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

### 配置服务器

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

### 启动服务

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

## 服务器构建器配置

`GrpcServerBuilder`提供了丰富的配置选项：

| 方法 | 描述 |
| --- | --- |
| `builder(int port)` | 创建指定端口的服务器构建器 |
| `maxRequestMessageLength(int)` | 设置最大请求消息长度 |
| `maxResponseMessageLength(int)` | 设置最大响应消息长度 |
| `enableHealthCheckService(boolean)` | 启用或禁用健康检查服务 |
| `enableHttpJsonTranscoding(boolean)` | 启用或禁用HTTP/JSON转码 |
| `enableStagingDocService(boolean)` | 在预发环境启用文档服务 |
| `requestMonitor(Consumer<RequestLog>)` | 设置请求监控回调 |
| `intercept(ServerInterceptor...)` | 添加服务拦截器 |
| `addService(BindableService)` | 添加gRPC服务实现 |
| `useBlockingTaskExecutor(boolean)` | 控制是否使用阻塞任务执行器 |
| `blockingTaskExecutor(int)` | 设置阻塞任务执行器线程数 |

## 客户端构建器配置

`GrpcClientBuilder`提供了以下配置选项：

| 方法 | 描述 |
| --- | --- |
| `builder(Class<T>)` | 创建指定Stub类型的客户端构建器 |
| `targetName(String)` | 设置服务目标名称，用于服务发现 |
| `endpoint(String, int)` | 设置服务端点主机和端口 |
| `maxInboundMessageSize(Integer)` | 设置最大接收消息大小 |
| `requestMonitor(Consumer<RequestLog>)` | 设置请求监控回调 |
| `scheme(Scheme)` | 设置协议方案 |

## 高级特性

### HTTP/JSON转码

FUST gRPC组件支持将gRPC服务转换为RESTful API，只需在构建服务器时启用：

```java
builder.enableHttpJsonTranscoding(true);
```

并在proto文件中添加相应注解：

```protobuf
service HelloService {
  rpc Hello (HelloRequest) returns (HelloResponse) {
    option (google.api.http) = {
      post: "/v1/hello"
      body: "*"
    };
  }
}
```

### 自定义拦截器

通过SPI机制扩展服务端拦截器：

1. 创建拦截器工厂实现：

```java
public class CustomInterceptorFactory implements GrpcServerInterceptorFactory {
    @Override
    public ServerInterceptor create() {
        return new ServerInterceptor() {
            @Override
            public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                    ServerCall<ReqT, RespT> call, 
                    Metadata headers,
                    ServerCallHandler<ReqT, RespT> next) {
                // 自定义拦截逻辑
                return next.startCall(call, headers);
            }
        };
    }
}
```

2. 创建SPI配置文件：
在`META-INF/services/com.zhihu.fust.armeria.grpc.server.GrpcServerInterceptorFactory`中添加实现类的完全限定名。

### 文档服务

FUST gRPC组件会在开发和测试环境自动启用文档服务，可通过以下属性配置：

- `armeria.rpc.doc.url.path`: 设置文档路径，默认为`_docs`
- `armeria.rpc.doc.disable`: 设置为`true`可禁用文档服务

## 最佳实践

### 服务定义

- 使用清晰、描述性的服务和方法名称
- 为服务和方法添加注释，便于文档生成
- 使用HTTP注解实现gRPC和REST双协议支持

### 错误处理

- 在服务实现中捕获异常，并通过`responseObserver.onError()`传递给客户端
- 使用适当的gRPC状态码表示不同类型的错误

### 性能优化

- 根据实际负载调整阻塞任务执行器线程数
- 对于高并发服务，考虑增加最大消息大小限制
- 在客户端使用适当的超时设置

### 观测性

- 配置请求监控回调以便记录请求指标
- 集成FUST可观测性组件实现分布式追踪
- 在生产环境中启用健康检查服务

## 故障排除

### 服务启动失败

- 检查端口是否被占用
- 确保服务实现正确实现了gRPC生成的基类

### 客户端连接失败

- 验证服务端点配置是否正确
- 检查网络连接和防火墙设置

### 消息大小错误

- 检查是否需要增加最大请求或响应消息大小
- 服务端设置：`builder.maxRequestMessageLength(10 * 1024 * 1024)`
- 客户端设置：`builder.maxInboundMessageSize(10 * 1024 * 1024)`

## 参考

- [Armeria gRPC文档](https://armeria.dev/docs/server-grpc)
- [gRPC官方文档](https://grpc.io/docs/)
- [FUST示例代码](https://github.com/zhihu/fust/tree/main/examples/fust-components-example/grpc)
