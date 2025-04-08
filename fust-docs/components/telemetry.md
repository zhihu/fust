# 可观测性

FUST 框架提供了一套基于 OpenTelemetry 的可观测性组件，支持分布式追踪、服务调用监控和指标收集等功能，帮助开发者更好地了解和监控应用程序的运行状态。

## 依赖配置

根据使用场景，FUST 提供了不同的可观测性组件，开发者可以根据需要选择合适的组件进行集成。

### Maven 依赖

#### 核心依赖

```xml
<!-- 可观测性 API -->
<dependency>
  <groupId>com.zhihu.fust</groupId>
  <artifactId>fust-telemetry-api</artifactId>
</dependency>

<!-- 可观测性 SDK 实现 -->
<dependency>
  <groupId>com.zhihu.fust</groupId>
  <artifactId>fust-telemetry-sdk</artifactId>
</dependency>
```

#### 框架集成

```xml
<!-- Spring MVC 集成 -->
<dependency>
  <groupId>com.zhihu.fust</groupId>
  <artifactId>fust-telemetry-spring-mvc</artifactId>
</dependency>

<!-- Redis Lettuce 客户端集成 -->
<dependency>
  <groupId>com.zhihu.fust</groupId>
  <artifactId>fust-telemetry-lettuce</artifactId>
</dependency>

<!-- MySQL 客户端集成 -->
<dependency>
  <groupId>com.zhihu.fust</groupId>
  <artifactId>fust-telemetry-mysql</artifactId>
</dependency>
```

### Gradle 依赖

#### 核心依赖

```groovy
// 可观测性 API
implementation 'com.zhihu.fust:fust-telemetry-api'

// 可观测性 SDK 实现
implementation 'com.zhihu.fust:fust-telemetry-sdk'
```

#### 框架集成

```groovy
// Spring MVC 集成
implementation 'com.zhihu.fust:fust-telemetry-spring-mvc'

// Redis Lettuce 客户端集成
implementation 'com.zhihu.fust:fust-telemetry-lettuce'

// MySQL 客户端集成
implementation 'com.zhihu.fust:fust-telemetry-mysql'
```

### Spring Boot 自动配置

如果你使用 FUST Boot，可以通过引入 `fust-boot-starter` 依赖来自动配置可观测性组件：

```xml
<dependency>
  <groupId>com.zhihu.fust</groupId>
  <artifactId>fust-boot-starter</artifactId>
</dependency>
```

## 特性概览

- **分布式追踪**：基于 OpenTelemetry 的分布式追踪能力，支持服务调用链路追踪
- **服务调用监控**：记录服务间调用情况，包括调用延迟、错误率等指标
- **自动集成**：与 Spring MVC、Redis、MySQL 等组件的自动集成
- **性能指标收集**：支持收集和导出应用程序性能指标
- **SPI 扩展机制**：通过 SPI 机制支持自定义扩展
- **上下文传播**：支持跨进程、线程的上下文传播

## 初始化配置

### 手动初始化

在非 Spring Boot 应用中，需要手动初始化可观测性组件：

```java
// 初始化环境
Env.init();
// 初始化遥测组件
TelemetryInitializer.init();
```

如果需要自定义遥测提供程序，可以通过 SPI 机制注册自定义的 `TelemetrySdkProvider` 实现：

```java
public class CustomTelemetrySdkProvider implements TelemetrySdkProvider {
    // 实现你的自定义遥测提供程序
}
```

然后在 `META-INF/services/com.zhihu.fust.provider.TelemetrySdkProvider` 文件中添加实现类的完全限定名。

### Spring Boot 自动配置

在 Spring Boot 应用中，FUST 提供了自动配置，无需手动初始化：

1. 引入 `fust-boot-starter` 依赖
2. 应用程序启动时，`ApplicationPreparedListener` 会自动初始化可观测性组件

## 使用方法

### 创建 Telemetry 实例

```java
// 创建遥测实例，传入instrumentation名称作为标识
Telemetry telemetry = Telemetry.create("your-service-name");
```

### 使用 Tracer 创建追踪

```java
// 获取 Tracer
Tracer tracer = telemetry.getTracer();

// 创建 Span
Span span = tracer.spanBuilder("your-operation-name")
    .setSpanKind(SpanKind.CLIENT)
    .startSpan();

try (Scope scope = span.makeCurrent()) {
    // 在此范围内执行操作
    // 当前上下文中可以访问这个 span
    doSomething();
} catch (Exception e) {
    span.recordException(e);
    span.setStatus(StatusCode.ERROR);
    throw e;
} finally {
    span.end(); // 结束 span
}
```

### 服务调用监控

FUST 提供了 `ServiceMeter` 用于监控服务调用情况：

```java
// 创建服务调用监控
ServiceMeter meter = telemetry.createServiceMeter(ServiceMeterKind.CLIENT);
meter.setMethod("yourMethod");           // 设置调用方法名
meter.setTargetService("targetService"); // 设置目标服务名
meter.setTargetMethod("targetMethod");   // 设置目标方法名

try {
    // 执行服务调用
    callService();
} catch (Exception e) {
    // 记录错误
    meter.setError(e);
    throw e;
} finally {
    // 结束监控并提交指标
    meter.end();
}
```

### 上下文传播

在分布式系统中，需要在服务间传播上下文信息：

```java
// 获取文本映射传播器
TextMapPropagator propagator = telemetry.getTextMapPropagator();

// 将当前上下文注入到载体中（如HTTP请求头）
propagator.inject(Context.current(), headers, (carrier, key, value) -> 
    carrier.put(key, value));

// 从载体中提取上下文（在接收端）
Context extractedContext = propagator.extract(Context.root(), headers, 
    (carrier, key) -> carrier.get(key));
```

### 服务入口标记

记录服务入口信息：

```java
// 获取当前服务入口
ServiceEntry entry = Telemetry.getServiceEntry();

// 创建新的服务入口
ServiceEntry newEntry = ServiceEntry.create("your-entry-point");
```

## 框架集成

### Spring MVC 集成

FUST 提供了与 Spring MVC 的集成，自动为 Web 请求创建追踪并记录调用指标：

```java
// 在 Spring MVC 配置类中注册拦截器
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SpringMvcTelemetryInterceptor())
                .addPathPatterns("/**");
    }
}
```

### Redis Lettuce 集成

FUST 提供了与 Redis Lettuce 客户端的集成，自动为 Redis 操作创建追踪：

```java
// 获取 Lettuce 遥测实例
Telemetry telemetry = LettuceTelemetry.getTelemetry();
```

### MySQL 集成

FUST 提供了与 MySQL 的集成，通过 `TracingQueryInterceptor` 自动为 SQL 查询创建追踪：

在 MySQL JDBC URL 中添加查询拦截器：

```
jdbc:mysql://localhost:3306/yourdb?queryInterceptors=com.zhihu.fust.telemetry.mysql.TracingQueryInterceptor
```

或在 MySQL DataSource 配置中添加：

```java
@Bean
public DataSource dataSource() {
    HikariDataSource dataSource = new HikariDataSource();
    // ... 其他配置
    dataSource.addDataSourceProperty("queryInterceptors", 
                                    "com.zhihu.fust.telemetry.mysql.TracingQueryInterceptor");
    return dataSource;
}
```

## 配置示例

### 典型的初始化流程

非 Spring Boot 应用中的典型初始化流程：

```java
public class Main {
    public static void main(String[] args) {
        // 初始化环境
        Env.init();
        // 初始化遥测组件
        TelemetryInitializer.init();
        // 初始化日志系统
        ILoggingSystem.get().initialize();
        
        // 应用程序代码...
    }
}
```

### Spring Boot 应用中的自动配置

Spring Boot 应用中，FUST 会自动配置可观测性组件，无需手动初始化：

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### gRPC 服务集成示例

```java
public class GrpcServer {
    public static void main(String[] args) {
        Env.init();
        TelemetryInitializer.init();
        ILoggingSystem.get().initialize();
        
        int port = 8010;
        GrpcServerBuilder builder = GrpcServerBuilder.builder(port);
        builder.requestMonitor(reqLog -> {
            long totalTimeMs = TimeUnit.NANOSECONDS.toMillis(reqLog.totalDurationNanos());
            RequestHeaders headers = reqLog.requestHeaders();
            log.warn("monitor|headers={} totalTimeMs={}ms", headers, totalTimeMs);
        });
        
        builder.addService(new YourServiceImpl())
               .build()
               .start()
               .join();
    }
}
```

## 高级特性

### 自定义 TelemetrySdkProvider

如果需要自定义可观测性配置，可以实现 `TelemetrySdkProvider` 接口：

```java
public class CustomTelemetrySdkProvider implements TelemetrySdkProvider {
    @Override
    public SdkTracerProvider sdkTracerProvider() {
        return SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(
                OtlpGrpcSpanExporter.builder()
                    .setEndpoint("your-collector-endpoint")
                    .build())
                .build())
            .build();
    }
    
    @Override
    public SdkMeterProvider sdkMeterProvider() {
        return SdkMeterProvider.builder()
            .registerMetricReader(PeriodicMetricReader.builder(
                OtlpGrpcMetricExporter.builder()
                    .setEndpoint("your-collector-endpoint")
                    .build())
                .build())
            .build();
    }
    
    @Override
    public SdkLoggerProvider sdkLoggerProvider() {
        return null; // 可选实现
    }
    
    @Override
    public TextMapPropagator textMapPropagator() {
        return TextMapPropagator.composite(
            W3CTraceContextPropagator.getInstance(), 
            W3CBaggagePropagator.getInstance());
    }
}
```

### 自定义服务指标收集器

如果需要自定义服务指标收集方式，可以实现 `ServiceMeterCollectorFactory` 接口：

```java
public class CustomServiceMeterCollectorFactory implements ServiceMeterCollectorFactory {
    @Override
    public ServiceMeterCollector create(String instrumentationName) {
        return new CustomServiceMeterCollector(instrumentationName);
    }
}
```

然后在 `META-INF/services/com.zhihu.fust.telemetry.api.ServiceMeterCollectorFactory` 文件中添加实现类的完全限定名。

## 最佳实践

1. **合理命名**：为服务、操作和指标使用清晰、一致的命名规范，便于后续分析和理解
2. **适当粒度**：选择合适的追踪粒度，过细会增加开销，过粗则无法有效定位问题
3. **捕获异常**：始终在捕获到异常时记录到追踪中，以便分析问题
4. **增加上下文信息**：在追踪中添加足够的上下文信息，如请求参数、用户ID等
5. **控制数据量**：避免在追踪中添加过大的数据，如完整的请求/响应体

## 故障排查

### 常见问题

1. **追踪丢失**：
   - 检查是否正确调用了 `span.end()`
   - 确认 OpenTelemetry 配置是否正确
   - 验证收集器是否正常运行

2. **上下文传播失败**：
   - 检查是否正确使用了 `propagator.inject()` 和 `propagator.extract()`
   - 确认传播的键值对是否被正确传递

3. **指标收集不完整**：
   - 检查 `ServiceMeter` 的使用是否正确
   - 确保在所有路径上都调用了 `meter.end()`

### 性能考虑

1. **采样率控制**：对于高流量系统，考虑使用采样策略减少数据量
2. **批处理导出**：使用批处理方式导出追踪和指标，减少网络开销
3. **缓冲区大小**：合理配置缓冲区大小，避免内存压力
4. **异步处理**：使用异步处理减少对主线程的影响

## 参考资料

- [OpenTelemetry 官方文档](https://opentelemetry.io/docs/)
- [FUST 示例代码](https://github.com/zhihu/fust/tree/main/examples)
- [W3C Trace Context 规范](https://www.w3.org/TR/trace-context/)
