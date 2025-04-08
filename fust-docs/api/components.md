# FUST Components API

FUST Components 是 FUST 框架的核心组件模块，提供了微服务开发中常用的功能实现，包括 gRPC 服务、配置中心集成、日志系统等。这些组件基于 FUST Core 构建，为应用开发提供了更高级别的抽象和便利性。

## 组件概览

FUST Components 包含以下主要组件：

- **fust-armeria-grpc**：基于 Armeria 的 gRPC 服务实现
- **fust-armeria-commons**：Armeria 相关的通用工具类
- **fust-logging-log4j2**：Log4j2 日志实现
- **fust-config-apollo**：Apollo 配置中心集成
- **fust-config-extension**：配置扩展功能

## gRPC 服务 (fust-armeria-grpc)

fust-armeria-grpc 组件提供了基于 Armeria 的 gRPC 服务实现，支持服务端和客户端的构建。

### 服务端 (Server)

服务端通过 `GrpcServerBuilder` 类构建 gRPC 服务器：

```java
import com.zhihu.fust.armeria.grpc.server.GrpcServerBuilder;
import io.grpc.BindableService;

// 创建 gRPC 服务器构建器
GrpcServerBuilder serverBuilder = GrpcServerBuilder.builder(8080)
    .maxRequestMessageLength(10 * 1024 * 1024)  // 设置最大请求消息大小为 10MB
    .enableHealthCheckService(true)             // 启用健康检查服务
    .enableHttpJsonTranscoding(true);           // 启用 HTTP/JSON 转码

// 添加 gRPC 服务实现
serverBuilder.addService(new MyServiceImpl());

// 构建并启动服务器
Server server = serverBuilder.build();
server.start().join();
```

#### 主要功能

- **HTTP/JSON 转码**：支持将 gRPC 服务转码为 HTTP/JSON API
- **健康检查**：内置健康检查服务
- **文档服务**：自动生成 API 文档
- **可观测性**：集成 FUST 的遥测组件
- **自定义拦截器**：支持添加自定义 gRPC 拦截器

#### 配置选项

`GrpcServerBuilder` 支持的主要配置选项：

| 方法 | 说明 | 默认值 |
| ---- | ---- | ------ |
| `maxRequestMessageLength(int)` | 设置最大请求消息大小 | -1 (不限制) |
| `enableHealthCheckService(boolean)` | 是否启用健康检查服务 | true |
| `enableHttpJsonTranscoding(boolean)` | 是否启用 HTTP/JSON 转码 | false |
| `enableStagingDocService(boolean)` | 是否在预发环境启用文档服务 | false |
| `blockingTaskExecutor(int)` | 设置阻塞任务线程池大小 | - |
| `intercept(ServerInterceptor...)` | 添加服务拦截器 | - |

### 客户端 (Client)

客户端通过 `GrpcClientBuilder` 类构建 gRPC 客户端：

```java
import com.zhihu.fust.armeria.grpc.client.GrpcClientBuilder;
import io.grpc.stub.AbstractStub;

// 创建 gRPC 客户端构建器
MyServiceGrpc.MyServiceStub stub = GrpcClientBuilder
    .builder(MyServiceGrpc.MyServiceStub.class)
    .targetName("my-service")               // 设置目标服务名
    // 或者直接指定端点
    // .endpoint("example.com", 8080)
    .maxInboundMessageSize(10 * 1024 * 1024)  // 设置最大接收消息大小为 10MB
    .build();

// 使用客户端发起调用
stub.myMethod(request, new StreamObserver<Response>() {
    @Override
    public void onNext(Response response) {
        // 处理响应
    }
    
    @Override
    public void onError(Throwable t) {
        // 处理错误
    }
    
    @Override
    public void onCompleted() {
        // 处理完成
    }
});
```

#### 主要功能

- **服务发现**：支持服务名解析
- **可观测性**：集成 FUST 的遥测组件
- **请求监控**：支持对请求进行监控

#### 配置选项

`GrpcClientBuilder` 支持的主要配置选项：

| 方法 | 说明 | 默认值 |
| ---- | ---- | ------ |
| `targetName(String)` | 设置目标服务名 | - |
| `endpoint(String, int)` | 设置目标端点 | - |
| `maxInboundMessageSize(Integer)` | 设置最大接收消息大小 | - |
| `requestMonitor(Consumer<RequestLog>)` | 设置请求监控器 | - |
| `scheme(Scheme)` | 设置通信协议 | HTTP |

## Armeria 通用组件 (fust-armeria-commons)

fust-armeria-commons 组件提供了 Armeria 相关的通用工具类：

### 请求日志装饰器

提供了客户端和服务端的请求日志装饰器：

```java
import com.zhihu.fust.armeria.commons.ArmeriaClientRequestLogDecorator;
import com.zhihu.fust.armeria.commons.ArmeriaServerRequestLogDecorator;

// 创建客户端请求日志装饰器
ArmeriaClientRequestLogDecorator clientDecorator = 
    new ArmeriaClientRequestLogDecorator(requestLog -> {
        // 自定义处理请求日志
    });

// 创建服务端请求日志装饰器
ArmeriaServerRequestLogDecorator serverDecorator = 
    new ArmeriaServerRequestLogDecorator(requestLog -> {
        // 自定义处理请求日志
    });
```

### Armeria 遥测

提供了 Armeria 相关的遥测工具：

```java
import com.zhihu.fust.armeria.commons.ArmeriaTelemetry;

// 获取遥测上下文
ServerMeterContext context = ArmeriaTelemetry
    .getMeterContextFromContext(serviceName, ctx);
```

## 日志系统 (fust-logging-log4j2)

fust-logging-log4j2 组件提供了 Log4j2 的集成实现，实现了 FUST Core 中定义的日志系统接口。

### 主要功能

- **日志级别动态调整**：支持在运行时调整日志级别
- **多环境配置**：支持不同环境的日志配置
- **日志扩展点**：提供日志系统的扩展能力

### 使用示例

```java
import com.zhihu.fust.core.logging.spi.ILoggingSystem;
import com.zhihu.fust.core.logging.LogLevel;

// 获取日志系统
ILoggingSystem loggingSystem = ILoggingSystem.get();

// 初始化日志系统
loggingSystem.initialize();

// 设置日志级别
loggingSystem.setLogLevel("com.example", LogLevel.DEBUG);
```

## 配置中心 (fust-config-apollo)

fust-config-apollo 组件提供了 Apollo 配置中心的集成实现，实现了 FUST Core 中定义的配置服务接口。

### 主要功能

- **配置实时更新**：支持配置的实时更新和监听
- **多环境配置**：支持不同环境的配置管理
- **配置文件格式**：支持多种配置文件格式（Properties、JSON、YAML 等）

### 使用示例

```java
import com.zhihu.fust.core.config.IConfigService;
import com.zhihu.fust.core.config.IConfigProperties;
import com.zhihu.fust.core.config.IConfigFile;
import com.zhihu.fust.core.config.ConfigFileFormatEnum;
import com.zhihu.fust.commons.lang.SpiServiceLoader;

// 获取配置服务
IConfigService configService = SpiServiceLoader.get(IConfigService.class).orElseThrow();

// 初始化配置服务
configService.initialize();

// 获取应用配置
IConfigProperties appConfig = configService.getAppConfig();

// 读取配置项
String serverPort = appConfig.getProperty("server.port", "8080");

// 获取 JSON 格式的配置文件
IConfigFile dbConfig = configService.getConfigFile("db", ConfigFileFormatEnum.JSON);
```

### 配置选项

Apollo 配置的主要选项：

| 配置项 | 说明 | 默认值 |
| ------ | ---- | ------ |
| `app.id` | Apollo 应用 ID | - |
| `apollo.meta` | Apollo Meta 服务地址 | - |
| `apollo.cacheDir` | Apollo 缓存目录 | - |
| `apollo.accesskey.secret` | Apollo 访问密钥 | - |

## 配置扩展 (fust-config-extension)

fust-config-extension 组件提供了配置的扩展功能，包括更便捷的配置访问、类型转换等。

### ConfigClient

`ConfigClient` 提供了更便捷的配置访问方式：

```java
import com.zhihu.fust.config.extension.ConfigClient;
import com.zhihu.fust.config.extension.DefaultConfigClient;
import com.fasterxml.jackson.core.type.TypeReference;

// 创建配置客户端
ConfigClient configClient = new DefaultConfigClient();

// 获取字符串配置
String value = configClient.getString("myKey");

// 获取整数配置
Integer intValue = configClient.getInteger("intKey");

// 获取对象配置
MyConfig config = configClient.get(MyConfig.class, "config");

// 获取列表配置
List<MyItem> items = configClient.listOf(
    new TypeReference<List<MyItem>>() {}, "items");

// 获取完整的 JSON 命名空间作为对象
MyNamespace namespace = configClient.getConfigByJsonNamespace(
    MyNamespace.class, "my-namespace");
```

### 灰度配置 (GrayConfig)

`GrayConfig` 提供了灰度配置的支持：

```java
import com.zhihu.fust.config.extension.GrayClient;
import com.zhihu.fust.config.extension.GrayConfig;

// 获取灰度配置
GrayClient grayClient = GrayClient.getInstance();
String value = grayClient.getConfig("my-key");

// 判断是否在灰度范围内
boolean isInGray = grayClient.isInGray("feature-flag");
```

## 最佳实践

### gRPC 服务

1. **服务定义**：使用 `.proto` 文件定义服务接口，保持简单明确
2. **健康检查**：启用健康检查服务，便于服务监控和负载均衡
3. **HTTP/JSON 转码**：对需要支持 HTTP 调用的服务启用 HTTP/JSON 转码
4. **服务文档**：在非生产环境启用文档服务，便于 API 调试
5. **自定义拦截器**：使用拦截器实现横切关注点，如认证、日志、监控等

### 配置管理

1. **命名空间划分**：按照功能模块划分配置命名空间，避免配置冲突
2. **配置监听**：对关键配置添加变更监听器，确保配置变更时正确处理
3. **默认值设计**：总是为配置项提供合理的默认值，增强系统的健壮性
4. **类型转换**：使用 ConfigClient 获取配置，自动完成类型转换
5. **灰度发布**：使用灰度配置控制功能的渐进式发布

### 日志管理

1. **日志级别**：根据环境设置合适的日志级别，生产环境通常使用 INFO 或更高级别
2. **动态调整**：利用 Log4j2 的动态配置能力，在需要时调整日志级别
3. **结构化日志**：使用结构化的日志格式，便于日志分析

## 扩展点

FUST Components 提供了多个扩展点，允许自定义组件行为：

### gRPC 服务扩展

- **GrpcServerInterceptorFactory**：自定义服务拦截器工厂
- **EndpointGroupBuilder**：自定义服务发现实现

### 配置扩展

- **ConfigPostProcessor**：配置处理后置处理器
- **ApolloMetaConfigProperties**：自定义 Apollo 元配置

### 日志扩展

- **Log4j2LoggingSystem**：自定义 Log4j2 日志系统行为
