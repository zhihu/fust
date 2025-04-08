# FUST Core API

FUST Core 是 FUST 框架的核心基础模块，提供了微服务应用开发的基础组件和工具类。该模块为其他 FUST 组件提供了统一的基础设施，包括环境管理、配置管理、日志系统以及通用工具类等。

## 核心特性

- **环境管理**：统一的环境抽象，支持多种环境类型（开发、测试、预发、生产等）
- **配置管理**：灵活的配置接口，支持多种配置源和动态配置更新
- **日志系统**：标准化的日志抽象，支持多种日志实现和动态日志级别调整
- **SPI 机制**：基于 Java SPI 的扩展机制，支持组件的可插拔设计
- **通用工具类**：提供字符串处理、集合操作、IO 操作等常用工具类

## 模块结构

FUST Core 包含以下主要模块：

- **fust-core**：核心接口和抽象类
- **fust-provider**：SPI 提供者接口
- **fust-commons**：通用工具类

## 环境管理 (Environment)

环境模块提供了统一的环境抽象，通过 `Env` 类可以获取当前应用的环境信息：

```java
import com.zhihu.fust.core.env.Env;

// 初始化环境（通常在应用启动时调用）
Env.init();

// 获取当前环境名称
String envName = Env.getName();

// 判断当前环境类型
if (Env.isDevelop()) {
    // 开发环境特定逻辑
} else if (Env.isProduction()) {
    // 生产环境特定逻辑
}

// 获取应用信息
String appName = Env.getAppName();
String serviceName = Env.getServiceName();
String version = Env.getVersion();
String instanceId = Env.getServiceInstanceId();
```

### 环境类型

FUST 定义了以下标准环境类型：

- **开发环境 (Development)**：本地开发环境
- **集成环境 (Integration)**：用于代码合并、单元测试和集成测试的 CI 环境
- **测试环境 (Testing)**：QA 测试环境
- **预发环境 (Staging)**：尽可能接近生产环境的预发布环境
- **生产环境 (Production)**：面向用户的正式环境

### 自定义环境提供者

你可以通过实现 `EnvironmentProvider` 接口来自定义环境信息提供者：

```java
import com.zhihu.fust.provider.EnvironmentProvider;

public class CustomEnvironmentProvider implements EnvironmentProvider {
    @Override
    public String getName() {
        return "dev";
    }
    
    @Override
    public boolean isDevelop() {
        return true;
    }
    
    // 实现其他必要方法...
}
```

然后通过 Java 的 SPI 机制注册你的自定义实现，在 `META-INF/services` 目录下创建名为 `com.zhihu.fust.provider.EnvironmentProvider` 的文件，内容为你的实现类的全限定名。

## 配置管理 (Configuration)

配置模块提供了统一的配置抽象，支持多种配置源和动态配置更新：

```java
import com.zhihu.fust.core.config.IConfigService;
import com.zhihu.fust.core.config.IConfigProperties;
import com.zhihu.fust.commons.lang.SpiServiceLoader;

// 获取配置服务
IConfigService configService = SpiServiceLoader.get(IConfigService.class)
                                      .orElseThrow(() -> new RuntimeException("No config service found"));

// 初始化配置服务（通常在应用启动时调用）
configService.initialize();

// 获取应用配置
IConfigProperties appConfig = configService.getAppConfig();

// 读取配置项
String serverPort = appConfig.getProperty("server.port", "8080");
Integer maxThreads = appConfig.getIntProperty("server.max-threads", 200);
Long timeout = appConfig.getLongProperty("client.timeout", 30000L);
```

### 配置监听器

你可以注册配置变更监听器，在配置变更时执行自定义逻辑：

```java
import com.zhihu.fust.core.config.IConfigPropertiesChangeListener;
import com.zhihu.fust.core.config.IConfigPropertiesChangeEvent;

appConfig.addChangeListener(new IConfigPropertiesChangeListener() {
    @Override
    public void onChange(IConfigPropertiesChangeEvent changeEvent) {
        // 处理配置变更事件
        if (changeEvent.getChange("server.port") != null) {
            // 处理端口变更
        }
    }
});
```

### 配置文件

FUST 支持多种格式的配置文件，可以通过 `ConfigFileFormatEnum` 指定格式：

```java
import com.zhihu.fust.core.config.ConfigFileFormatEnum;
import com.zhihu.fust.core.config.IConfigFile;

// 获取 JSON 格式的配置文件
IConfigFile dbConfig = configService.getConfigFile("db", ConfigFileFormatEnum.JSON);

// 获取配置文件内容
String content = dbConfig.getContent();
```

## 日志系统 (Logging)

日志模块提供了统一的日志抽象，支持多种日志实现和动态日志级别调整：

```java
import com.zhihu.fust.core.logging.spi.ILoggingSystem;
import com.zhihu.fust.core.logging.LogLevel;

// 获取日志系统
ILoggingSystem loggingSystem = ILoggingSystem.get();

// 初始化日志系统（通常在应用启动时调用）
loggingSystem.initialize();

// 设置日志级别
loggingSystem.setLogLevel("com.example", LogLevel.DEBUG);

// 获取日志配置
LoggerConfiguration loggerConfig = loggingSystem.getLoggerConfiguration("com.example");
```

### 日志根名称

FUST 使用 `ROOT` 作为根日志记录器的名称：

```java
import static com.zhihu.fust.core.logging.spi.ILoggingSystem.ROOT_LOGGER_NAME;

// 设置根日志级别
loggingSystem.setLogLevel(ROOT_LOGGER_NAME, LogLevel.INFO);
```

## 通用工具类 (Commons)

FUST Commons 模块提供了一系列通用工具类，简化常见操作：

### SPI 服务加载器

```java
import com.zhihu.fust.commons.lang.SpiServiceLoader;

// 加载 SPI 服务
Optional<MyService> service = SpiServiceLoader.get(MyService.class);
```

### 字符串工具

```java
import com.zhihu.fust.commons.lang.StringUtils;

// 判断字符串是否为空
boolean isEmpty = StringUtils.isEmpty("test");

// 判断字符串是否为空白
boolean isBlank = StringUtils.isBlank("  ");

// 字符串连接
String joined = StringUtils.join(new String[]{"a", "b", "c"}, ",");
```

### 类工具

```java
import com.zhihu.fust.commons.lang.ClassUtils;

// 获取类的简单名称
String simpleName = ClassUtils.getSimpleName(MyClass.class);

// 判断类是否存在
boolean exists = ClassUtils.isPresent("com.example.MyClass");
```

## 最佳实践

### 环境管理

1. **统一环境判断**：使用 `Env` 类来判断当前环境，避免使用自定义环境变量或系统属性
2. **早期初始化**：在应用启动的早期阶段调用 `Env.init()` 确保环境信息正确初始化
3. **日志环境标记**：在日志中包含环境信息，便于问题排查

### 配置管理

1. **默认值设计**：总是为配置项提供合理的默认值，增强系统的健壮性
2. **配置分组**：按照功能或模块将配置项分组到不同的命名空间
3. **监听重要配置**：为关键配置项添加变更监听器，确保配置变更时能够正确处理

### 日志管理

1. **适当的日志级别**：根据环境设置适当的日志级别，开发环境可以更详细，生产环境更精简
2. **结构化日志**：使用结构化的日志格式，便于日志收集和分析
3. **关键信息记录**：记录关键业务操作和重要系统事件，便于审计和问题排查

### 工具类使用

1. **优先使用框架工具**：优先使用 FUST 提供的工具类，保持代码风格一致
2. **避免重复实现**：避免重复实现 FUST Commons 已经提供的功能
3. **扩展而非修改**：需要自定义功能时，通过扩展而非修改现有工具类
