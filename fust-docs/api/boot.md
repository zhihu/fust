# FUST Boot API

FUST Boot 是基于 Spring Boot 构建的微服务框架，提供了一套完整的企业级应用开发组件。通过 FUST Boot，您可以快速构建基于 Spring Boot 的微服务应用，同时享受 FUST 框架提供的各种增强功能。

## 核心特性

- **自动配置**：FUST Boot 提供丰富的自动配置能力，无需复杂的配置即可使用 FUST 框架的各个组件
- **简化开发**：通过 starter 模块，简化依赖管理和组件集成
- **性能优化**：针对微服务场景进行了性能优化，提供更高效的组件实现
- **可观测性**：集成 OpenTelemetry，支持分布式追踪、服务监控和日志收集
- **标准化**：提供标准化的微服务组件，便于团队协作和项目维护

## 依赖配置

### Maven

```xml
<!-- 添加 FUST Boot BOM -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.zhihu.fust</groupId>
            <artifactId>fust-boot-bom</artifactId>
            <version>${fust.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<!-- 添加基础 Starter -->
<dependency>
    <groupId>com.zhihu.fust</groupId>
    <artifactId>fust-boot-starter</artifactId>
</dependency>
```

### Gradle

```groovy
// 添加 FUST Boot BOM
implementation platform("com.zhihu.fust:fust-boot-bom:${fustVersion}")

// 添加基础 Starter
implementation 'com.zhihu.fust:fust-boot-starter'
```

## 核心模块

### fust-boot-starter

基础 Starter 模块，提供 FUST 框架的核心功能和自动配置。当您添加此依赖时，将自动引入以下功能：

- 环境初始化 (`Env.init()`)
- 遥测组件初始化 (`TelemetryInitializer.init()`)
- 日志系统初始化 (`ILoggingSystem.initialize()`)
- 配置服务初始化 (`IConfigService.initialize()`)

### fust-boot-web

Web 应用开发支持模块，基于 Spring Boot Web 构建，提供以下功能：

- 替换默认的 Tomcat 为 Jetty 容器
- 集成 FUST 的 Web 组件，提供增强的 Web 开发体验
- 自动配置 Spring MVC

依赖配置：

```xml
<dependency>
    <groupId>com.zhihu.fust</groupId>
    <artifactId>fust-boot-web</artifactId>
</dependency>
```

### fust-boot-grpc

gRPC 服务支持模块，基于 Armeria 构建，提供高性能的 gRPC 服务实现。

依赖配置：

```xml
<dependency>
    <groupId>com.zhihu.fust</groupId>
    <artifactId>fust-boot-grpc</artifactId>
</dependency>
```

### fust-boot-jdbc

JDBC 数据库访问支持模块，提供增强的 JDBC 功能。

依赖配置：

```xml
<dependency>
    <groupId>com.zhihu.fust</groupId>
    <artifactId>fust-boot-jdbc</artifactId>
</dependency>
```

### fust-boot-mybatis

MyBatis 集成模块，提供与 MyBatis 的无缝集成。特性包括：

- 自动配置 SqlSessionFactory
- 支持自定义拦截器
- 数据源适配与连接策略

依赖配置：

```xml
<dependency>
    <groupId>com.zhihu.fust</groupId>
    <artifactId>fust-boot-mybatis</artifactId>
</dependency>
```

### fust-boot-jedis

Redis 客户端 Jedis 集成模块，提供 Jedis 的自动配置。

依赖配置：

```xml
<dependency>
    <groupId>com.zhihu.fust</groupId>
    <artifactId>fust-boot-jedis</artifactId>
</dependency>
```

### fust-boot-lettuce

Redis 客户端 Lettuce 集成模块，提供 Lettuce 的自动配置。Lettuce 是一个基于 Netty 的高性能 Redis 客户端。

依赖配置：

```xml
<dependency>
    <groupId>com.zhihu.fust</groupId>
    <artifactId>fust-boot-lettuce</artifactId>
</dependency>
```

### fust-boot-log4j2

Log4j2 日志集成模块，提供 Log4j2 的自动配置。

依赖配置：

```xml
<dependency>
    <groupId>com.zhihu.fust</groupId>
    <artifactId>fust-boot-log4j2</artifactId>
</dependency>
```

## 配置文件

**重要说明**：FUST 框架使用 JSON 格式的配置文件，而不是 Spring Boot 的 YAML 或 Properties 格式。这些 JSON 配置文件应放置在项目的 `src/main/resources` 目录下。

### 配置文件命名规则

FUST 配置文件遵循以下命名规则：`{component}-{env}.json`，其中：
- `{component}` 是组件名称，如 `db`、`redis` 等
- `{env}` 是当前环境名称，如 `dev`、`test`、`prod` 等

例如：`db-dev.json`、`redis-prod.json`

### 数据库配置 (JDBC/MyBatis)

数据库配置文件为 `db-{env}.json`，支持单数据源、多数据源和读写分离：

```json
[
  {
    "name": "db1",
    "defaultDatabase": true,
    "ds": [
      {
        "name": "db1-master",
        "url": "jdbc:mysql://localhost:3306/db1",
        "username": "root",
        "password": "123456",
        "type": "master",
        "driverClassName": "com.mysql.cj.jdbc.Driver",
        "connectionTimeout": 30000,
        "idleTimeout": 600000,
        "maxLifetime": 1800000,
        "maximumPoolSize": 10,
        "minimumIdle": 5
      },
      {
        "name": "db1-slave",
        "url": "jdbc:mysql://replica.example.com:3306/db1",
        "username": "readonly",
        "password": "123456",
        "type": "slave"
      }
    ]
  }
]
```

配置参数说明：
- `name`: 数据库名称
- `defaultDatabase`: 是否为默认数据库
- `ds`: 数据源配置列表
  - `name`: 数据源名称
  - `url`: JDBC 连接 URL
  - `username`: 数据库用户名
  - `password`: 数据库密码
  - `type`: 数据源类型，可选值为 `master` 或 `slave`
  - 其他参数直接传递给 HikariCP 连接池

### Redis 配置 (Jedis/Lettuce)

Redis 配置文件为 `redis-{env}.json`，支持单节点、多节点和集群模式：

```json
[
  {
    "name": "default",
    "defaultConnection": true,
    "commandTimeout": 1000,
    "connectionTimeout": 2000,
    "readFrom": "masterPreferred",
    "nodes": [
      {
        "host": "localhost",
        "port": 6379,
        "password": "",
        "type": "primary"
      },
      {
        "host": "replica.example.com",
        "port": 6379,
        "type": "replica"
      }
    ]
  }
]
```

配置参数说明：
- `name`: Redis 实例名称
- `defaultConnection`: 是否为默认连接
- `commandTimeout`: 命令执行超时时间（毫秒）
- `connectionTimeout`: 连接超时时间（毫秒）
- `readFrom`: 读取策略，可选值为 `masterPreferred`、`nearest`、`replica`、`master`
- `nodes`: Redis 节点列表
  - `host`: Redis 主机地址
  - `port`: Redis 端口
  - `password`: Redis 认证密码
  - `type`: 节点类型，可选值为 `primary` 或 `replica`

## 自动配置机制

FUST Boot 采用 Spring Boot 的自动配置机制，通过在 `META-INF/spring.factories` 文件中注册组件来实现自动配置。主要的自动配置包括：

### 应用程序生命周期监听器

FUST Boot 注册了 `ApplicationPreparedListener`，用于在应用程序启动过程中初始化 FUST 框架的核心组件：

```java
public class ApplicationPreparedListener implements ApplicationListener<ApplicationPreparedEvent> {
    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        Env.init();                          // 初始化环境
        TelemetryInitializer.init();         // 初始化遥测组件
        ILoggingSystem loggingSystem = ILoggingSystem.get();
        SpiServiceLoader.get(IConfigService.class)
                .ifPresent(IConfigService::initialize);  // 初始化配置服务
        loggingSystem.initialize();          // 初始化日志系统
    }
}
```

## 使用示例

### 创建 FUST Boot 应用

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 配置和使用 Redis

1. 创建 `redis-dev.json` 配置文件：

```json
[
  {
    "name": "default",
    "nodes": [
      {
        "host": "localhost",
        "port": 6379
      }
    ]
  }
]
```

2. 在代码中使用 Redis：

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisController {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @GetMapping("/redis/{key}")
    public String getValue(@PathVariable String key) {
        return redisTemplate.opsForValue().get(key);
    }
}
```

### 配置和使用 MyBatis

1. 创建 `db-dev.json` 配置文件：

```json
[
  {
    "name": "db1",
    "ds": [
      {
        "name": "db1-master",
        "url": "jdbc:mysql://localhost:3306/db1",
        "username": "root",
        "password": "123456"
      }
    ]
  }
]
```

2. 创建实体类和 Mapper：

```java
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Mapper
interface UserMapper {
    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Long id);
}

@RestController
public class UserController {
    
    @Autowired
    private UserMapper userMapper;
    
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return userMapper.findById(id);
    }
}
```

## 最佳实践

1. **使用 BOM 管理依赖**：通过 `fust-boot-bom` 管理依赖版本，避免版本冲突

2. **按需添加组件**：只添加需要的组件依赖，减少不必要的依赖

3. **使用 JSON 配置文件**：确保将组件配置放在合适的 JSON 文件中，如 `db-dev.json`、`redis-dev.json` 等

4. **环境隔离**：为不同环境（开发、测试、生产）创建不同的配置文件，使用环境变量确定当前环境

5. **使用自动配置**：充分利用 FUST Boot 的自动配置能力，减少手动配置

6. **集成可观测性组件**：在生产环境中，集成 FUST 的可观测性组件，便于问题排查和性能优化
