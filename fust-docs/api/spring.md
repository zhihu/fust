# FUST Spring API

FUST Spring 是 FUST 框架的 Spring 集成模块，为使用 Spring 框架的应用提供增强功能。它包含了数据库访问、缓存操作、Web 开发等常用功能的增强实现，使开发者能够更简单高效地使用 Spring 技术栈。

## 模块概览

FUST Spring 包含以下主要模块：

- **fust-spring-jdbc**：JDBC 数据源和连接管理
- **fust-spring-mybatis**：MyBatis 增强功能
- **fust-spring-redis-common**：Redis 操作的通用抽象
- **fust-spring-jedis**：基于 Jedis 的 Redis 客户端实现
- **fust-spring-lettuce**：基于 Lettuce 的 Redis 客户端实现
- **fust-spring-web**：Spring Web 增强功能
- **fust-spring-toolkit**：通用工具类

## JDBC 数据源 (fust-spring-jdbc)

fust-spring-jdbc 模块提供了灵活的 JDBC 数据源管理，支持多数据源、读写分离等高级特性。

### 主要功能

- **多数据源支持**：轻松配置和管理多个数据源
- **读写分离**：自动根据 SQL 类型路由到主库或从库
- **数据源自动发现**：基于配置文件自动发现和配置数据源
- **连接池集成**：集成 HikariCP 作为默认连接池实现
- **连接跟踪**：支持对数据库连接进行跟踪和监控

### 核心类和接口

- **`ConnectionStrategy`**：连接策略接口，定义获取数据库连接的方式
- **`JdbcConnectionStrategy`**：连接策略的默认实现，支持基于 SQL 的读写分离
- **`DataSourceAdapter`**：数据源适配器，封装单个数据源的主从配置
- **`HikariDataSourceCreator`**：HikariCP 数据源创建器
- **`JdbcConnectionFactory`**：JDBC 连接工厂
- **`DataSourceFileProvider`**：数据源配置文件提供者

### 使用示例

#### 1. 配置数据源（在 `db-dev.json` 中）

```json
[
  {
    "name": "db1",
    "defaultDatabase": true,
    "ds": [
      {
        "name": "db1-master",
        "url": "jdbc:mysql://master.example.com:3306/db1",
        "username": "root",
        "password": "123456",
        "type": "master"
      },
      {
        "name": "db1-slave",
        "url": "jdbc:mysql://slave.example.com:3306/db1",
        "username": "readonly",
        "password": "123456",
        "type": "slave"
      }
    ]
  }
]
```

#### 2. 创建连接策略

```java
import com.zhihu.fust.spring.jdbc.JdbcConnectionStrategy;
import com.zhihu.fust.spring.jdbc.DataSourceAdapter;
import java.util.List;

// 获取数据源适配器列表（通常通过配置文件加载）
List<DataSourceAdapter> adapters = ...;

// 创建连接策略
JdbcConnectionStrategy strategy = new JdbcConnectionStrategy("db1", adapters);

// 获取连接
Connection conn = strategy.getConnection(sql);

// 或者明确指定主库连接
Connection masterConn = strategy.getMasterConnection();
```

## MyBatis 增强 (fust-spring-mybatis)

fust-spring-mybatis 模块提供了对 MyBatis 的增强功能，简化持久层开发。

### 主要功能

- **通用 DAO 接口**：提供 TemplateDao 通用接口，减少重复代码
- **批量操作支持**：内置批量插入和批量更新支持
- **分片更新**：支持 patch 方式的部分字段更新
- **拦截器扩展**：支持自定义拦截器，实现 AOP 功能
- **自动表元数据**：自动生成表元数据，简化 SQL 操作

### 核心类和接口

- **`TemplateDao<T>`**：通用 DAO 接口，提供基础 CRUD 操作
- **`TableMeta`**：表元数据，包含表结构信息
- **`DefaultExecutorInterceptor`**：默认的执行器拦截器
- **`DefaultConfiguration`**：MyBatis 默认配置

### 使用示例

#### 1. 创建实体类

```java
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Table(name = "user")
public class User {
    @Id
    private Long id;
    private String username;
    private String email;
}
```

#### 2. 定义 DAO 接口

```java
import com.zhihu.fust.spring.mybatis.TemplateDao;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDao extends TemplateDao<User> {
    // 通过继承 TemplateDao，自动获得基本的 CRUD 方法
    // 可以添加自定义方法
    User findByUsername(String username);
}
```

#### 3. 使用 DAO 接口

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserDao userDao;
    
    public User getUser(Long id) {
        return userDao.find(id);
    }
    
    public boolean createUser(User user) {
        return userDao.create(user);
    }
    
    public boolean updateUserPartially(User user) {
        // 只更新非 null 字段
        return userDao.patch(user);
    }
    
    public boolean createUsers(List<User> users) {
        return userDao.batchCreate(users);
    }
}
```

## Redis 操作 (fust-spring-redis-common, fust-spring-jedis, fust-spring-lettuce)

FUST Spring 提供了对 Redis 的全面支持，包括通用抽象和两种客户端实现（Jedis 和 Lettuce）。

### 主要功能

- **多客户端支持**：同时支持 Jedis 和 Lettuce 两种客户端
- **统一接口**：提供统一的 API 接口，便于切换客户端实现
- **主从配置**：支持 Redis 主从配置
- **连接池管理**：自动管理连接池
- **配置文件支持**：通过配置文件自动配置 Redis
- **命令跟踪**：支持 Redis 命令的跟踪和监控

### 核心类和接口

- **`RedisFactoryConfig`**：Redis 连接工厂配置接口
- **`JedisConfigFactory`**：Jedis 连接工厂实现
- **`LettuceConfigFactory`**：Lettuce 连接工厂实现
- **`DefaultRedisProperties`**：Redis 配置属性
- **`DefaultRedisPropertiesListReader`**：Redis 配置文件读取器

### 使用示例

#### 1. 配置 Redis（在 `redis-dev.json` 中）

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
        "host": "redis-master.example.com",
        "port": 6379,
        "password": "password",
        "type": "primary"
      },
      {
        "host": "redis-replica.example.com",
        "port": 6379,
        "type": "replica"
      }
    ]
  }
]
```

#### 2. 使用 Jedis 客户端

```java
import com.zhihu.fust.spring.jedis.JedisConfigFactory;
import com.zhihu.fust.spring.redis.common.DefaultRedisProperties;
import com.zhihu.fust.spring.redis.common.RedisNodeTypeEnum;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

// 获取 Redis 配置列表（通常通过配置文件加载）
List<DefaultRedisProperties> propertiesList = ...;

// 创建 Jedis 配置工厂
JedisConfigFactory factory = new JedisConfigFactory(propertiesList);

// 获取默认连接工厂
RedisConnectionFactory connectionFactory = factory.getDefault();

// 或者获取指定名称的连接工厂
RedisConnectionFactory namedFactory = factory.get("cache", RedisNodeTypeEnum.MASTER);

// 创建 Redis 模板
StringRedisTemplate template = new StringRedisTemplate(connectionFactory);
template.opsForValue().set("key", "value");
```

#### 3. 使用 Lettuce 客户端

```java
import com.zhihu.fust.spring.redis.lettuce.LettuceConfigFactory;
import com.zhihu.fust.spring.redis.common.DefaultRedisProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

// 获取 Redis 配置列表（通常通过配置文件加载）
List<DefaultRedisProperties> propertiesList = ...;

// 创建 Lettuce 配置工厂
LettuceConfigFactory factory = new LettuceConfigFactory(propertiesList);

// 获取默认连接工厂
RedisConnectionFactory connectionFactory = factory.getDefault();

// 或者获取指定名称的连接工厂（Lettuce 自动处理主从读写分离）
RedisConnectionFactory namedFactory = factory.get("cache");

// 创建 Redis 模板
StringRedisTemplate template = new StringRedisTemplate(connectionFactory);
template.opsForValue().set("key", "value");
```

## Web 开发 (fust-spring-web)

fust-spring-web 模块提供了对 Spring Web 的增强功能，统一了 REST API 的响应格式和异常处理。

### 主要功能

- **统一响应格式**：统一成功和错误响应的格式
- **全局异常处理**：提供全局异常处理机制
- **API 异常体系**：提供结构化的 API 异常体系
- **请求参数处理**：增强的请求参数处理
- **响应处理**：增强的响应处理

### 核心类和接口

- **`RestSuccessResponse`**：REST 成功响应封装
- **`RestErrorResponse`**：REST 错误响应封装
- **`RestExceptionHandlerExceptionResolver`**：REST 异常处理解析器
- **`ApiException`**：API 异常基类
- **`ErrorInfo`**：错误信息封装

### 使用示例

#### 1. 使用统一响应格式

```java
import com.zhihu.fust.spring.web.RestSuccessResponse;
import com.zhihu.fust.spring.web.RestErrorResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    public RestSuccessResponse<User> getUser(@PathVariable Long id) {
        User user = userService.getUser(id);
        return new RestSuccessResponse<>(user);
    }
    
    @PostMapping
    public RestSuccessResponse<User> createUser(@RequestBody User user) {
        userService.createUser(user);
        return new RestSuccessResponse<>(user);
    }
    
    @ExceptionHandler(Exception.class)
    public RestErrorResponse handleException(Exception e) {
        return new RestErrorResponse("USER_ERROR", e.getMessage());
    }
}
```

#### 2. 使用 API 异常

```java
import com.zhihu.fust.spring.web.ApiException;
import com.zhihu.fust.spring.web.ErrorInfo;

public class UserNotFoundException extends ApiException {
    
    public UserNotFoundException(Long userId) {
        super(new ErrorInfo("USER_NOT_FOUND", "User not found with ID: " + userId));
    }
}

@Service
public class UserService {
    
    public User getUser(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }
}
```

## 工具类 (fust-spring-toolkit)

fust-spring-toolkit 模块提供了常用的工具类，简化开发工作。

### 主要功能

- **Bean 复制工具**：简化 Bean 之间的属性复制
- **日志工具**：增强的日志工具

### 核心类

- **`BeanCopyUtils`**：Bean 复制工具类

### 使用示例

```java
import com.zhihu.fust.spring.toolkit.beans.BeanCopyUtils;

// 从源对象复制到目标对象
UserDTO userDTO = new UserDTO();
BeanCopyUtils.copyProperties(user, userDTO);

// 创建新对象并复制属性
UserDTO dto = BeanCopyUtils.copyProperties(user, UserDTO.class);

// 复制指定字段
BeanCopyUtils.copyProperties(source, target, "id", "name", "email");

// 忽略指定字段
BeanCopyUtils.copyPropertiesIgnore(source, target, "password", "salt");
```

## 最佳实践

### 数据库访问

1. **使用读写分离**：对于读多写少的应用，利用 FUST 的读写分离功能分担数据库负载
2. **合理配置连接池**：根据实际负载情况配置合适的连接池参数
3. **使用 TemplateDao**：优先使用 TemplateDao 接口减少重复代码
4. **批量操作优化**：使用批量操作 API 优化大批量数据处理
5. **部分更新**：使用 patch 方法进行部分字段更新，减少不必要的字段修改

### Redis 操作

1. **选择合适的客户端**：Lettuce 适合高并发场景，Jedis 更为轻量
2. **配置读写分离**：合理配置 Redis 读写分离，减轻主节点负担
3. **设置合理的超时时间**：为 Redis 操作设置合理的超时时间，避免长时间阻塞
4. **使用连接池**：使用连接池管理 Redis 连接，避免频繁创建和销毁连接
5. **数据序列化**：选择合适的序列化方式，平衡性能和兼容性

### Web 开发

1. **统一响应格式**：使用统一的响应格式，便于前端处理
2. **细化异常类型**：根据业务需求细化异常类型，便于精确定位问题
3. **合理设置错误码**：设计合理的错误码体系，便于排查问题
4. **记录关键日志**：在关键处理点记录充分的日志信息，便于问题定位

## 扩展点

FUST Spring 提供了多个扩展点，允许自定义组件行为：

### JDBC 扩展

- **`DataSourceFileProvider`**：自定义数据源配置文件提供者
- **`DataSourceDiscover`**：自定义数据源发现机制

### MyBatis 扩展

- **`DefaultExecutorInterceptor`**：自定义 SQL 执行拦截器

### Redis 扩展

- **`RedisConfigFileProvider`**：自定义 Redis 配置文件提供者
- **`RedisResourceDiscover`**：自定义 Redis 资源发现机制

### Web 扩展

- **`DefaultApiFactory`**：自定义 API 工厂实现
