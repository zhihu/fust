# Redis集成

本章将介绍如何在FUST框架中集成Redis缓存，以提高应用性能。我们将使用FUST框架提供的Redis支持模块，简化Redis的配置和使用。

## Redis支持概述

FUST框架对Redis的支持主要包括两个部分：

1. **fust-boot-jedis**：基于Jedis客户端的Redis集成
2. **fust-boot-lettuce**：基于Lettuce客户端的Redis集成

两者都提供了类似的功能，但有一些区别：

- **Jedis**：较为轻量，使用阻塞I/O，适用于单线程场景
- **Lettuce**：基于Netty的异步驱动，支持非阻塞I/O，适用于高并发场景

本章我们将以Lettuce为例，演示如何在应用中集成Redis。

## 添加Redis依赖

首先，在`demo-yoda-business`模块的`pom.xml`文件中添加FUST Redis支持依赖：

```xml
<!-- 使用Lettuce客户端 -->
<dependency>
    <groupId>com.zhihu.fust</groupId>
    <artifactId>fust-boot-lettuce</artifactId>
</dependency>

<!-- 或者，如果您更习惯使用Jedis客户端 -->
<!-- 
<dependency>
    <groupId>com.zhihu.fust</groupId>
    <artifactId>fust-boot-jedis</artifactId>
</dependency>
-->
```

> 注意：您只需要添加其中一个依赖，不需要额外引入Lettuce或Jedis库，FUST框架会自动管理相关依赖。

## 配置Redis

FUST框架使用JSON文件来配置Redis连接，这样可以更好地管理多环境配置。

在`demo-yoda-business`模块的`src/main/resources`目录下创建`redis-dev.json`文件：

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

这个配置定义了一个名为`default`的Redis连接，连接到本地的Redis服务器。

### Redis配置说明

Redis配置文件支持以下主要属性：

1. `name`: Redis连接的逻辑名称
2. `nodes`: Redis节点列表
3. `password`: Redis密码（如果有）
4. `database`: 使用的数据库索引，默认为0
5. `timeout`: 连接超时时间
6. `poolConfig`: 连接池配置

如果需要配置Redis集群或哨兵模式，可以在`nodes`中添加多个节点。

### 多环境配置

与数据库配置类似，FUST框架会根据当前环境自动加载对应的Redis配置文件：

- 开发环境：`redis-dev.json`
- 测试环境：`redis-test.json`
- 生产环境：`redis-prod.json`

可以通过系统属性`env.name`或环境变量`ENV_NAME`来指定当前环境。

## 注入RedisTemplate

FUST框架会自动配置`StringRedisTemplate`，可以直接在服务类中注入使用：

```java
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;
    
    public void setKey(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }
    
    public String getKey(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}
```

## 为UserService添加缓存支持

现在，我们将扩展`UserService`，添加对Redis缓存的支持。

首先，修改`UserService`接口，添加`findByCache`方法：

```java
/**
 * 从缓存中获取用户，如果不存在则从数据库加载并缓存
 * 
 * @param userId 用户ID
 * @return 用户信息
 */
Optional<UserModel> findByCache(long userId);
```

然后，在`UserServiceImpl`类中实现该方法，使用`ObjectMapper`来进行JSON序列化：

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;

// ... 其他import ...

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserDao userDao;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String USER_CACHE_KEY_PREFIX = "user:";
    private static final long USER_CACHE_EXPIRE_TIME = 30; // 30分钟
    
    // ... 其他方法 ...
    
    @Override
    public Optional<UserModel> findByCache(long userId) {
        String key = USER_CACHE_KEY_PREFIX + userId;
        String json = redisTemplate.opsForValue().get(key);
        
        if (json != null) {
            try {
                log.info("Found user in cache: {}", userId);
                return Optional.of(objectMapper.readValue(json, UserModel.class));
            } catch (Exception e) {
                log.error("Failed to deserialize user from cache: {}", userId, e);
                // 缓存数据有问题，删除缓存
                redisTemplate.delete(key);
            }
        }
        
        // 缓存不存在或反序列化失败，从数据库查询
        UserModel user = userDao.find(userId);
        if (user != null) {
            try {
                String userJson = objectMapper.writeValueAsString(user);
                redisTemplate.opsForValue().set(key, userJson, USER_CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
                log.info("Cached user: {}", userId);
                return Optional.of(user);
            } catch (Exception e) {
                log.error("Failed to serialize user to cache: {}", userId, e);
            }
            return Optional.of(user);
        }
        
        return Optional.empty();
    }
}
```

此外，我们还需要修改用户更新和删除方法，确保在更新和删除用户时清除缓存：

```java
@Override
@Transactional
public boolean updateUser(UserModel user) {
    log.info("Updating user: {}", user.getId());
    boolean result = userDao.update(user);
    if (result) {
        String key = USER_CACHE_KEY_PREFIX + user.getId();
        redisTemplate.delete(key);
        log.info("Deleted user cache after update: {}", user.getId());
    }
    return result;
}

@Override
@Transactional
public boolean deleteUser(Long id) {
    log.info("Deleting user: {}", id);
    boolean result = userDao.remove(id);
    if (result) {
        String key = USER_CACHE_KEY_PREFIX + id;
        redisTemplate.delete(key);
        log.info("Deleted user cache after removal: {}", id);
    }
    return result;
}
```

## 测试缓存功能

最后，我们编写测试用例验证缓存功能。

编写测试类：

```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceRedisTest {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Test
    @Transactional
    void testFindByCache() {
        // 创建一个新用户
        UserModel user = new UserModel();
        user.setBirthday(LocalDate.of(1990, 1, 1));
        user.setName("Cache Test User");
        boolean created = userService.createUser(user);
        assertTrue(created);
        
        // 第一次查询，应该从数据库加载并缓存
        Optional<UserModel> result1 = userService.findByCache(user.getId());
        assertTrue(result1.isPresent());
        
        // 验证缓存是否存在
        String key = "user:" + user.getId();
        assertTrue(redisTemplate.hasKey(key));
        
        // 第二次查询，应该从缓存加载
        Optional<UserModel> result2 = userService.findByCache(user.getId());
        assertTrue(result2.isPresent());
        
        // 更新用户，缓存应该被清除
        user.setName("Updated Cache User");
        userService.updateUser(user);
        assertFalse(redisTemplate.hasKey(key));
        
        // 再次查询，应该重新缓存
        Optional<UserModel> result3 = userService.findByCache(user.getId());
        assertTrue(result3.isPresent());
        assertEquals("Updated Cache User", result3.get().getName());
        
        // 删除用户，缓存应该被清除
        userService.deleteUser(user.getId());
        assertFalse(redisTemplate.hasKey(key));
    }
}
```

## 配置ObjectMapper

为了保证JSON序列化和反序列化的一致性，我们还应该配置一个统一的`ObjectMapper`。

在`demo-yoda-business`模块中创建`src/main/java/demo/yoda/business/config/JacksonConfig.java`文件：

```java
package demo.yoda.business.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
```

这个配置确保`LocalDate`和`LocalDateTime`等Java 8日期时间类型能够正确序列化和反序列化。

## 总结

在本章中，我们学习了如何在FUST框架中集成Redis缓存：

1. 添加FUST Redis支持依赖（Lettuce或Jedis）
2. 使用JSON文件配置Redis连接
3. 注入和使用StringRedisTemplate
4. 为UserService添加缓存支持
5. 实现缓存更新和删除
6. 编写测试用例验证缓存功能

通过添加Redis缓存，我们减少了数据库访问，提高了应用性能。在大规模应用中，合理使用缓存是提升性能的关键策略之一。在下一章中，我们将学习如何开发HTTP服务，为前端或其他系统提供REST API接口。 