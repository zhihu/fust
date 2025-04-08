# 日志系统

FUST 框架提供了功能强大且高度可配置的日志系统，基于 Log4j2 实现，同时支持与 Apollo 配置中心集成以实现动态日志级别调整。本文档将介绍如何在 FUST 应用中配置和使用日志系统。

## 特性概览

- 基于 Log4j2 的高性能日志系统
- 支持控制台和文件输出
- 自动整合 OpenTelemetry 的链路追踪信息
- 支持通过配置文件进行静态配置
- 支持通过 Apollo 配置中心进行动态配置
- 支持按环境加载不同的日志配置
- 提供简洁的日志级别管理 API

## 快速开始

### 基础配置

在 FUST 应用中，日志系统会在应用启动时自动初始化。默认情况下，日志系统会根据当前环境自动配置适当的日志级别：

- 开发环境：默认 INFO 级别
- 生产环境：默认 WARN 级别

### 使用日志

在代码中使用 SLF4J 接口进行日志记录：

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyService {
    private static final Logger logger = LoggerFactory.getLogger(MyService.class);
    
    public void doSomething() {
        logger.info("This is an info message");
        logger.warn("This is a warning message");
        logger.error("This is an error message");
        
        // 使用占位符
        logger.info("Processing user: {}", userId);
        
        // 记录异常
        try {
            // 业务逻辑
        } catch (Exception e) {
            logger.error("Operation failed", e);
        }
    }
}
```

### 链路追踪集成

FUST 的日志系统自动集成了 OpenTelemetry 的链路追踪信息，可以在日志中包含 traceId：

```java
import com.zhihu.fust.telemetry.api.TraceUtils;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

public class MyService {
    private static final Logger logger = LoggerFactory.getLogger(MyService.class);
    
    public void processRequest() {
        // 确保有 traceId
        Context context = TraceUtils.ensureTraceId();
        try (Scope scope = context.makeCurrent()) {
            logger.info("Processing request with traceId: {}", TraceUtils.getTraceId());
            // 业务逻辑
        }
    }
}
```

## 配置详解

### 日志配置文件

FUST 支持通过属性文件配置日志级别。创建 `log.properties` 文件并放置在应用的 resources 目录下：

```properties
# 设置根日志级别
root.level=INFO

# 设置特定包或类的日志级别
logger.com.zhihu.fust.examples=DEBUG
logger.com.zhihu.fust.examples.LogTest=WARN
```

### 环境变量和系统属性

可以通过环境变量或系统属性覆盖日志配置：

| 配置项 | 环境变量 | 系统属性 | 描述 |
| ----- | ------- | ------- | ---- |
| 根日志级别 | LOG_ROOT_LEVEL | log.root.level | 设置根日志级别 |
| 控制台日志级别 | LOG_CONSOLE_LEVEL | log.console.level | 设置控制台输出的日志级别 |
| 文件日志级别 | LOG_FILE_LEVEL | log.file.level | 设置文件输出的日志级别 |
| 日志文件目录 | LOG_FILE_DIR | log.file.dir | 设置日志文件存储目录 |
| 日志配置目录 | LOG_CFG_DIR | log.cfg.dir | 设置日志配置文件存储目录 |

### 日志级别

FUST 支持以下日志级别（按严重程度递增）：

- TRACE：最详细的日志级别，用于跟踪程序执行流程
- DEBUG：调试信息，帮助开发人员调试应用
- INFO：普通信息，记录应用正常运行状态
- WARN：警告信息，表示潜在的问题
- ERROR：错误信息，表示出现了错误但应用仍可继续运行
- FATAL：致命错误，表示严重错误导致应用无法继续运行
- OFF：关闭所有日志

## Apollo 配置中心集成

### 初始化配置

FUST 可以与 Apollo 配置中心集成，实现动态日志配置。首先确保 Apollo 客户端配置正确：

```java
// Apollo 会在应用启动时自动初始化
@Component
public class AppConfig {
    @Value("${app.id}")
    private String appId;
    
    @PostConstruct
    public void init() {
        System.setProperty("app.id", appId);
        // Apollo 其他配置...
    }
}
```

### 动态调整日志级别

在 Apollo 配置中心创建 `logging` 命名空间，并添加相应的配置项：

```properties
# 调整根日志级别
root.level=INFO

# 调整特定包的日志级别
logger.com.zhihu.fust.examples=DEBUG
```

这些配置会在运行时动态应用，无需重启应用。

## 编程方式调整日志级别

在应用中，可以通过编程方式动态调整日志级别：

```java
import com.zhihu.fust.core.logging.LogLevel;
import com.zhihu.fust.core.logging.spi.ILoggingSystem;

@Service
public class LoggingService {
    public void setLogLevel(String loggerName, String level) {
        ILoggingSystem loggingSystem = ILoggingSystem.get();
        LogLevel logLevel = LogLevel.valueOf(level.toUpperCase());
        loggingSystem.setLogLevel(loggerName, logLevel);
    }
    
    public void setRootLogLevel(String level) {
        setLogLevel(ILoggingSystem.ROOT_LOGGER_NAME, level);
    }
}
```

## 日志输出格式

### 默认日志格式

FUST 的日志系统默认使用以下格式输出日志：

```
[日期时间] [日志级别] [线程名] [traceId] [类名] - 日志内容
```

例如：

```
[2023-03-14 15:21:30,123] [INFO] [main] [4f21b5b2a3c78d9e] [com.zhihu.fust.examples.Main] - Processing request
```

### 自定义日志格式

可以通过修改 Log4j2 配置模板来自定义日志格式。Log4j2 配置通常存储在应用的 `generated` 目录下，格式为 XML。

## 最佳实践

### 日志级别选择

1. **TRACE/DEBUG**：仅在开发和测试环境使用，生产环境应避免使用这两个级别，以免产生过多日志
2. **INFO**：记录重要的业务事件，可在开发和测试环境广泛使用，生产环境需适度使用
3. **WARN**：记录需要注意但不是错误的情况，如性能下降、接近限制等
4. **ERROR**：记录真正的错误情况，应包含详细的上下文信息和堆栈跟踪
5. **FATAL**：仅用于记录导致应用终止的严重错误

### 日志内容建议

1. **包含上下文信息**：记录足够的上下文信息，如用户ID、请求ID、操作类型等
2. **避免敏感信息**：不要记录密码、令牌等敏感信息
3. **使用结构化日志**：使用键值对格式，如 `key1=value1, key2=value2`，便于后续分析
4. **包含异常堆栈**：记录异常时，应包含完整的异常堆栈
5. **使用占位符**：使用 SLF4J 的占位符语法，而不是字符串拼接，提高性能

## 故障排查

### 常见问题

1. **日志级别配置不生效**：检查环境变量、系统属性设置是否正确
2. **日志文件未生成**：检查文件路径权限和磁盘空间
3. **日志内容不完整**：检查日志格式配置和缓冲区设置
4. **日志性能问题**：检查是否使用了异步日志配置

### 诊断步骤

1. 检查应用启动日志，查看日志系统初始化信息
2. 确认日志配置文件位置和内容
3. 验证环境变量和系统属性设置
4. 检查 Apollo 配置中心的配置项
5. 使用 JMX 监控工具查看 Log4j2 的运行状态
