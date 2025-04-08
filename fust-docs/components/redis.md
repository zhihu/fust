# Redis 支持

FUST 框架提供了强大的 Redis 支持模块，支持多实例、主从架构等高级特性，同时简化了 Redis 的配置和使用方式。本文档将介绍如何在 FUST 应用中配置和使用 Redis。

## 依赖配置

FUST 提供了两种 Redis 客户端实现：基于 Lettuce 的实现和基于 Jedis 的实现。您可以根据项目需求选择合适的实现。

### Maven 依赖

#### 使用 Lettuce 客户端（推荐）

Lettuce 是一个基于 Netty 的高性能异步 Redis 客户端，支持高级特性如连接池、SSL、集群等，是 FUST 框架中推荐的 Redis 客户端。

```xml
<!-- 在 Spring Boot 应用中使用 Lettuce -->
<dependency>
    <groupId>com.zhihu.fust</groupId>
    <artifactId>fust-boot-lettuce</artifactId>
</dependency>

<!-- 在非 Spring Boot 应用中使用 Lettuce -->
<dependency>
    <groupId>com.zhihu.fust</groupId>
    <artifactId>fust-spring-lettuce</artifactId>
</dependency>
```

#### 使用 Jedis 客户端

Jedis 是一个简单易用的 Redis 客户端，适用于对 Redis 操作简单的应用场景。

```xml
<!-- 在 Spring Boot 应用中使用 Jedis -->
<dependency>
    <groupId>com.zhihu.fust</groupId>
    <artifactId>fust-boot-jedis</artifactId>
</dependency>

<!-- 在非 Spring Boot 应用中使用 Jedis -->
<dependency>
    <groupId>com.zhihu.fust</groupId>
    <artifactId>fust-spring-jedis</artifactId>
</dependency>
```

### Gradle 依赖

#### 使用 Lettuce 客户端（推荐）

```groovy
// 在 Spring Boot 应用中使用 Lettuce
implementation 'com.zhihu.fust:fust-boot-lettuce'

// 在非 Spring Boot 应用中使用 Lettuce
implementation 'com.zhihu.fust:fust-spring-lettuce'
```

#### 使用 Jedis 客户端

```groovy
// 在 Spring Boot 应用中使用 Jedis
implementation 'com.zhihu.fust:fust-boot-jedis'

// 在非 Spring Boot 应用中使用 Jedis
implementation 'com.zhihu.fust:fust-spring-jedis'
```

### Lettuce 与 Jedis 的选择

- **Lettuce（推荐）**:
  - 基于 Netty 的异步非阻塞实现
  - 支持主从读写分离策略（通过 `readFrom` 配置）
  - 支持 Redis Cluster
  - 线程安全，支持多线程并发访问
  - 更高的性能和更低的资源消耗

- **Jedis**:
  - 同步阻塞 API
  - 简单易用，学习曲线低
  - 不支持主从读写分离策略，需要显式指定节点类型
  - 需要显式配置连接池

一般情况下，建议使用 Lettuce 客户端，除非您有特殊需求或已有基于 Jedis 的代码需要迁移。

### 自动配置

在 Spring Boot 应用中，引入 `fust-boot-lettuce` 或 `fust-boot-jedis` 依赖后，FUST 框架会自动配置 Redis 连接工厂和模板：

- `RedisConnectionFactory` - Redis 连接工厂，用于创建 Redis 连接
- `RedisTemplate` - Redis 操作模板，用于执行 Redis 命令
- `StringRedisTemplate` - 专门用于处理字符串类型的 Redis 操作模板

自动配置会使用 `redis-{env}.json` 配置文件中的设置来初始化这些 Bean。

## 特性概览

- 支持多 Redis 实例配置
- 支持主从节点配置
- 灵活的读写策略配置
- 基于 Lettuce 客户端的高性能连接
- 可自定义连接池配置
- 支持连接超时和命令超时配置
- 内置 TCP Keep-Alive 支持

## 配置方式

### 基础配置

FUST 使用 JSON 格式的配置文件来管理 Redis 连接。默认配置文件名为 `redis-{env}.json`，其中 `{env}` 是当前环境名称（如 dev、integration、testing、staging、production）。

一个最基本的 Redis 配置示例如下：

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

这个配置创建了一个名为 "default" 的 Redis 实例，连接到本地的 6379 端口。

### 完整配置选项

以下是 Redis 配置的完整选项：

```json
[
  {
    "name": "cache",
    "defaultConnection": true,
    "commandTimeout": 500,
    "connectionTimeout": 1000,
    "readFrom": "masterPreferred",
    "eagerInitialization": true,
    "validateConnection": true,
    "tcpUserTimeout": 60000,
    "enableKeepAlive": true,
    "keepAliveOptions": {
      "idle": 30000,
      "interval": 5000,
      "count": 3
    },
    "nodes": [
      {
        "host": "redis-master.example.com",
        "port": 6379,
        "password": "secret",
        "type": "primary"
      },
      {
        "host": "redis-replica1.example.com",
        "port": 6379,
        "type": "replica"
      }
    ]
  }
]
```

#### 实例配置选项：

| 参数 | 说明 | 默认值 |
| ---- | ---- | ------ |
| name | Redis 实例名称，用于区分多个实例 | 必填 |
| defaultConnection | 是否为默认连接 | false |
| commandTimeout | 命令执行超时时间（毫秒） | 500 |
| connectionTimeout | 连接超时时间（毫秒） | 1000 |
| readFrom | 读取策略，可选值：masterPreferred（优先读主库）、nearest（就近读取）、replica（只读从库）、master（只读主库） | masterPreferred |
| eagerInitialization | 是否预热 Lettuce 连接 | true |
| validateConnection | 获取连接时是否验证连接有效性 | true |
| tcpUserTimeout | TCP_USER_TIMEOUT 设置（毫秒）, -1 表示不开启超时配置 | 60000 |
| enableKeepAlive | 是否开启 TCP Keep-Alive | true |
| keepAliveOptions | Keep-Alive 相关配置 | - |
| nodes | Redis 节点列表 | 必填 |

#### 节点配置选项：

| 参数 | 说明 | 默认值 |
| ---- | ---- | ------ |
| host | Redis 主机地址 | 必填 |
| port | Redis 端口 | 必填 |
| password | Redis 认证密码 | "" |
| type | 节点类型，可选值：primary（主节点）、replica（从节点） | primary |

### 多实例配置

FUST 支持配置多个 Redis 实例，每个实例可以有不同的配置：

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
  },
  {
    "name": "session",
    "nodes": [
      {
        "host": "redis-session.example.com",
        "port": 6379
      }
    ]
  }
]
```

## Spring中的Redis实例配置

FUST提供了`RedisFactoryConfig`接口及其实现类`LettuceConfigFactory`和`JedisConfigFactory`，方便在Spring Configuration中管理和获取不同的Redis连接实例。

### RedisFactoryConfig接口

该接口提供了以下核心方法：

```java
public interface RedisFactoryConfig {
    /**
     * 获取默认的RedisConnectionFactory
     */
    RedisConnectionFactory getDefault();

    /**
     * 获取指定名称的RedisConnectionFactory
     * Lettuce支持readFrom策略
     */
    RedisConnectionFactory get(String name);

    /**
     * 获取指定名称和类型的RedisConnectionFactory
     * Jedis不支持readFrom策略，需要明确指定节点类型
     */
    RedisConnectionFactory get(String name, RedisNodeTypeEnum type);
}
```

### 在Spring配置中使用

以下是一个Spring Configuration示例，展示如何利用`RedisFactoryConfig`来配置多个`StringRedisTemplate`：

```java
@Configuration
public class RedisConfiguration {
    
    private final RedisFactoryConfig redisFactoryConfig;
    
    public RedisConfiguration(RedisFactoryConfig redisFactoryConfig) {
        this.redisFactoryConfig = redisFactoryConfig;
    }
    
    /**
     * 默认的StringRedisTemplate
     */
    @Bean
    @Primary
    public StringRedisTemplate stringRedisTemplate() {
        return new StringRedisTemplate(redisFactoryConfig.getDefault());
    }
    
    /**
     * 基于名称获取特定的StringRedisTemplate
     */
    @Bean
    public StringRedisTemplate sessionRedisTemplate() {
        return new StringRedisTemplate(redisFactoryConfig.get("session"));
    }
    
    /**
     * 获取指定名称的主节点连接的StringRedisTemplate
     */
    @Bean
    public StringRedisTemplate cacheMasterRedisTemplate() {
        return new StringRedisTemplate(
            redisFactoryConfig.get("cache", RedisNodeTypeEnum.MASTER)
        );
    }
    
    /**
     * 获取指定名称的从节点连接的StringRedisTemplate
     * 注意：仅Jedis需要明确指定节点类型，Lettuce会根据readFrom策略自动路由
     */
    @Bean
    public StringRedisTemplate cacheReplicaRedisTemplate() {
        return new StringRedisTemplate(
            redisFactoryConfig.get("cache", RedisNodeTypeEnum.REPLICA)
        );
    }
}
```

### Lettuce与Jedis的区别

在FUST中，默认使用Lettuce作为Redis客户端，它支持读写分离策略（通过`readFrom`配置）：

- 使用`LettuceConfigFactory`时，`get(String name)`方法返回的连接工厂会根据配置的`readFrom`策略自动路由请求。
- 使用`JedisConfigFactory`时，由于Jedis不支持读写分离策略，需要通过`get(String name, RedisNodeTypeEnum type)`明确指定要连接的节点类型（主节点或从节点）。

例如，对于主从架构的Redis：

```java
// 使用Lettuce - 自动根据readFrom配置路由请求
StringRedisTemplate lettuceTemplate = new StringRedisTemplate(
    lettuceConfigFactory.get("cache")
);

// 使用Jedis - 需要明确指定节点类型
StringRedisTemplate jedisMasterTemplate = new StringRedisTemplate(
    jedisConfigFactory.get("cache", RedisNodeTypeEnum.MASTER)
);
StringRedisTemplate jedisReplicaTemplate = new StringRedisTemplate(
    jedisConfigFactory.get("cache", RedisNodeTypeEnum.REPLICA)
);
```

### 自定义RedisTemplate

除了使用`StringRedisTemplate`，你也可以自定义`RedisTemplate`来支持不同类型的值序列化：

```java
@Bean
public RedisTemplate<String, Object> customRedisTemplate() {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(redisFactoryConfig.getDefault());
    
    // 使用Jackson2JsonRedisSerializer作为值序列化器
    Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
    ObjectMapper mapper = new ObjectMapper();
    mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, 
                               ObjectMapper.DefaultTyping.NON_FINAL);
    serializer.setObjectMapper(mapper);
    
    // 使用StringRedisSerializer作为键序列化器
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(serializer);
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(serializer);
    
    template.afterPropertiesSet();
    return template;
}
```

## 使用方式

### 基础使用

在 Spring Boot 应用中，可以直接注入 `StringRedisTemplate` 或 `RedisTemplate` 来使用默认的 Redis 实例：

```java
@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public void cacheData(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public String getData(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}
```

### 示例代码

以下是一个简单的 Redis 服务示例：

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public void cacheHello(String name, String time) {
        redisTemplate.opsForValue().set(name, time);
    }

    public String getHelloTime(String name) {
        return redisTemplate.opsForValue().get(name);
    }

    public void test() {
        redisTemplate.opsForValue().set("t1", "test1");
        var s = redisTemplate.opsForValue().get("t1");
        log.info(s);
    }
}
```

### 多实例访问

如果配置了多个 Redis 实例，可以通过 `@Qualifier` 注解指定要使用的实例：

```java
@Service
public class MultiRedisService {

    private final StringRedisTemplate defaultRedisTemplate;
    private final StringRedisTemplate sessionRedisTemplate;

    public MultiRedisService(
            StringRedisTemplate defaultRedisTemplate,
            @Qualifier("sessionRedisTemplate") StringRedisTemplate sessionRedisTemplate) {
        this.defaultRedisTemplate = defaultRedisTemplate;
        this.sessionRedisTemplate = sessionRedisTemplate;
    }

    public void saveToDefault(String key, String value) {
        defaultRedisTemplate.opsForValue().set(key, value);
    }

    public void saveToSession(String key, String value) {
        sessionRedisTemplate.opsForValue().set(key, value);
    }
}
```

## 高级特性

### 主从配置

对于读多写少的场景，可以配置主从架构以提高性能：

```json
[
  {
    "name": "cache",
    "readFrom": "nearest",
    "nodes": [
      {
        "host": "redis-master.example.com",
        "port": 6379,
        "type": "primary"
      },
      {
        "host": "redis-replica1.example.com",
        "port": 6379,
        "type": "replica"
      },
      {
        "host": "redis-replica2.example.com",
        "port": 6379,
        "type": "replica"
      }
    ]
  }
]
```

### 读写策略

通过 `readFrom` 参数，可以控制读取操作的路由策略：

- `masterPreferred`：优先从主节点读取，主节点不可用时从从节点读取
- `nearest`：从延迟最低的节点读取
- `replica`：只从从节点读取
- `master`：只从主节点读取

### 连接池配置

对于使用 Jedis 客户端的场景，可以配置连接池参数：

```json
{
  "name": "pool-example",
  "pool": {
    "maxTotal": 8,
    "maxIdle": 8,
    "minIdle": 0,
    "maxWaitMillis": -1,
    "timeBetweenEvictionRunsMillis": 30000,
    "numTestsPerEvictionRun": -1,
    "minEvictableIdleTimeMillis": 60000,
    "softMinEvictableIdleTimeMillis": 1800000,
    "testOnCreate": false,
    "testOnBorrow": false,
    "testOnReturn": false,
    "testWhileIdle": true,
    "blockWhenExhausted": true
  }
}
```

## 最佳实践

1. **合理设置超时时间**：为避免长时间阻塞，建议设置合理的命令超时和连接超时时间
2. **使用连接池**：对于高并发场景，合理配置连接池参数可以提高性能
3. **使用主从架构**：对于读多写少的场景，配置主从架构并使用适当的读策略可以分散负载
4. **监控连接状态**：启用 Keep-Alive 配置可以及时发现连接异常
5. **预热连接**：对于关键应用，建议启用连接预热（eagerInitialization）以减少首次访问延迟

## 故障排查

### 常见问题

1. **连接超时**：检查网络连通性、防火墙设置以及 Redis 服务器状态
2. **认证失败**：确认配置的密码是否正确
3. **命令执行超时**：检查 Redis 服务器负载，可能需要调整 `commandTimeout` 参数
4. **连接池耗尽**：对于高并发场景，可能需要增加连接池配置中的 `maxTotal` 参数

### 诊断方法

可以通过以下方法诊断 Redis 连接问题：

1. 检查应用日志中的 Redis 相关错误
2. 使用 Redis CLI 工具直接连接 Redis 服务器，验证连通性
3. 使用网络工具（如 telnet）检查网络连接状态
4. 监控 Redis 服务器的性能指标，如内存使用、命令执行时间等
