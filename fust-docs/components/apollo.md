# Apollo 配置中心

FUST 框架与 Apollo 配置中心进行了深度集成，提供了统一的配置管理解决方案，实现配置的集中管理、动态更新和不同环境隔离等功能。本文档将介绍如何在 FUST 应用中使用 Apollo 配置中心。

## 特性概览

- 集中管理配置，避免散落在各个项目的配置问题
- 支持配置的动态更新，无需重启应用
- 支持不同环境（DEV/FAT/UAT/PRO）的配置隔离
- 支持配置的继承和覆盖
- 支持灰度发布
- 提供统一的配置操作接口
- 支持多种配置文件格式（Properties、XML、JSON、YAML、TXT 等）
- 支持配置的实时监控和历史追溯

## 快速开始

### 1. 项目依赖

在 Maven 项目中添加依赖：

```xml
<dependency>
    <groupId>com.zhihu.fust</groupId>
    <artifactId>fust-config-apollo</artifactId>
    <version>${fust.version}</version>
</dependency>
```

或者在 Gradle 项目中：

```groovy
implementation 'com.zhihu.fust:fust-config-apollo:${fust.version}'
```

### 2. 基础配置

在项目的资源目录中创建 `config-{env}.properties` 文件（例如 `config-dev.properties`）：

```properties
# 应用ID，必须与Apollo配置中心的应用ID一致
app.id=your-app-id
# Apollo配置中心地址
config.server=http://apollo-config-server:8080
```

### 3. 使用配置

在 FUST 应用中，可以通过 `IConfigService` 接口访问配置：

```java
import com.zhihu.fust.commons.lang.SpiServiceLoader;
import com.zhihu.fust.core.config.IConfigProperties;
import com.zhihu.fust.core.config.IConfigService;
import com.zhihu.fust.core.env.Env;

public class MyService {
    private IConfigService configService;
    
    public MyService() {
        // 在FUST应用中，通常不需要手动初始化，框架会自动处理
        Env.init();
        configService = SpiServiceLoader.get(IConfigService.class).orElse(null);
        if (configService != null) {
            configService.initialize();
        }
    }
    
    public void readConfig() {
        IConfigProperties appConfig = configService.getAppConfig();
        
        // 读取配置项，支持默认值
        String serverUrl = appConfig.getProperty("server.url", "http://localhost:8080");
        int timeout = appConfig.getIntProperty("http.timeout", 5000);
        boolean enabled = appConfig.getBooleanProperty("feature.enabled", false);
        
        // 使用配置项...
    }
}
```

## Spring Boot 集成

在 Spring Boot 应用中，FUST 会自动初始化 Apollo 配置，无需额外的代码：

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

你可以直接通过 `@Value` 注解或 `@ConfigurationProperties` 注解注入配置：

```java
@Component
public class MyComponent {
    @Value("${server.url}")
    private String serverUrl;
    
    @Value("${http.timeout:5000}")
    private int timeout;
    
    // ...
}
```

## 配置详解

### 配置文件优先级

FUST 会按照以下优先级加载 Apollo 配置：

1. 系统属性（`System.getProperty()`）
2. 环境变量
3. `config-{env}.properties` 文件
4. 默认配置

### 主要配置项

| 配置项 | 环境变量 | 系统属性 | 描述 |
| ----- | ------ | ------ | ---- |
| 应用ID | APP_ID | app.id | Apollo 配置中心的应用ID |
| 配置中心地址 | CONFIG_SERVER | config.server | Apollo 配置中心的服务地址 |
| 配置缓存目录 | CONFIG_CACHE_DIR | config.cache.dir | 本地配置缓存目录，默认为 `{generated_dir}/apollo-cache` |
| 访问密钥 | CONFIG_ACCESS_KEY | config.access.key | 访问 Apollo 配置中心的密钥 |

### 配置命名空间（Namespace）

Apollo 使用命名空间来组织不同类型的配置。FUST 提供了便捷的 API 来访问不同命名空间的配置：

```java
// 获取应用默认命名空间（application）的配置
IConfigProperties appConfig = configService.getAppConfig();

// 获取指定命名空间的配置
IConfigProperties dbConfig = configService.getConfig("db");
IConfigProperties redisConfig = configService.getConfig("redis");
```

### 配置文件支持

FUST 支持通过 Apollo 管理各种格式的配置文件：

```java
// 自动识别文件格式
IConfigFile logbackConfig = configService.getConfigFile("logback.xml");

// 指定文件格式
IConfigFile jsonConfig = configService.getConfigFile("config", ConfigFileFormatEnum.JSON);
```

## 配置监听与动态更新

### 属性变更监听

监听配置变更并自动响应：

```java
IConfigProperties config = configService.getConfig("application");
config.addChangeListener(changeEvent -> {
    for (String key : changeEvent.changedKeys()) {
        IConfigPropertiesChange change = changeEvent.getChange(key);
        System.out.println(String.format(
            "配置 %s 已更改: %s -> %s",
            key, change.getOldValue(), change.getNewValue()
        ));
    }
});
```

### 配置文件变更监听

```java
IConfigFile configFile = configService.getConfigFile("logback.xml");
configFile.addChangeListener(changeEvent -> {
    String newContent = changeEvent.getNewValue();
    System.out.println("配置文件已更新，新内容: " + newContent);
    // 重新加载日志配置...
});
```

## Apollo 控制台使用指南

### 本地开发环境部署

FUST 提供了基于 Docker 的快速部署脚本，方便本地开发和测试：

```bash
# x86架构
docker-compose -f docker-compose.yml up

# arm64架构（如M1/M2 Mac）
docker-compose -f docker-compose-arm64.yml up
```

启动后可访问：
- 配置中心: http://localhost:8080
- 管理门户: http://localhost:8070

默认登录账号：
- 用户名: apollo
- 密码: admin

### 配置操作指南

1. **创建应用**：在管理门户创建与 `app.id` 一致的应用
2. **创建命名空间**：可创建私有或公共命名空间
3. **添加配置项**：在相应命名空间下添加配置项
4. **发布配置**：填写变更说明并发布
5. **查看历史**：跟踪配置变更历史

## 最佳实践

### 配置分类

- **应用配置**：特定于应用的配置，放在应用默认命名空间
- **公共配置**：多应用共享的配置，放在公共命名空间
- **数据库配置**：数据库相关配置，独立命名空间管理
- **第三方服务配置**：如缓存、消息队列等，独立命名空间管理
- **日志配置**：日志相关配置，单独命名空间管理

### 配置结构化

对于复杂配置，建议使用 JSON 或 YAML 格式，提高可读性和维护性：

```java
// JSON配置示例
IConfigFile jsonConfig = configService.getConfigFile("service-config.json");
String jsonContent = jsonConfig.getContent();
// 使用Jackson或Gson等库解析
```

### 敏感信息处理

敏感信息（如密码、密钥）建议：
1. 使用 Apollo 的访问控制功能限制查看权限
2. 考虑使用加密存储

## 故障排查

### 常见问题

1. **配置无法获取**：检查应用ID、配置中心地址是否正确
2. **本地缓存问题**：检查缓存目录权限，尝试清除缓存
3. **配置未生效**：检查是否已发布配置，及命名空间是否正确
4. **环境不匹配**：确认当前运行环境与Apollo环境一致

### 排查步骤

1. 检查日志中的Apollo相关信息
2. 验证配置文件和环境变量设置
3. 通过Apollo控制台查看配置发布状态
4. 检查网络连接是否正常

### 常见日志错误

- `No DataSource, skip configService initialization`：未配置Apollo数据源
- `Get Apollo Config failed`：连接Apollo服务器失败
- `Cannot find config for namespace`：命名空间不存在
- `No available config server`：配置服务不可用

## 进阶功能

### 多环境配置

FUST 会根据当前运行环境自动加载相应的 Apollo 配置：

```java
// 根据Env.getName()确定环境
String env = Env.getName(); // 如DEV, FAT, UAT, PRO
```

### 配置密钥访问控制

为提高安全性，可以配置访问密钥：

```properties
config.access.key=your-access-key
```

### 自定义配置提供者

实现 `ConfigCustomProvider` 接口，可以自定义配置文件路径和命名规则：

```java
public class MyConfigProvider implements ConfigCustomProvider {
    @Override
    public String configFile() {
        return "classpath:my-apollo-config.properties";
    }
    
    @Override
    public String envFormatFile() {
        return "classpath:my-apollo-config-%s.properties";
    }
    
    // 实现其他方法...
}
```
