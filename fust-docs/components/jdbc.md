# JDBC 支持

FUST 框架提供了强大的 JDBC 支持模块，它简化了数据库连接管理、提供了主从分离和读写分离功能，并支持灵活的连接策略配置。本文档将介绍如何在 FUST 应用中配置和使用 JDBC 组件。

## 依赖配置

### Maven 依赖

```xml
<!-- 在 Spring Boot 应用中使用 JDBC -->
<dependency>
    <groupId>com.zhihu.fust</groupId>
    <artifactId>fust-boot-jdbc</artifactId>
</dependency>

<!-- 在非 Spring Boot 应用中使用 JDBC -->
<dependency>
    <groupId>com.zhihu.fust</groupId>
    <artifactId>fust-spring-jdbc</artifactId>
</dependency>
```

### Gradle 依赖

```groovy
// 在 Spring Boot 应用中使用 JDBC
implementation 'com.zhihu.fust:fust-boot-jdbc'

// 在非 Spring Boot 应用中使用 JDBC
implementation 'com.zhihu.fust:fust-spring-jdbc'
```

## 特性概览

- **主从分离**：支持配置主库和从库，根据SQL类型自动路由
- **读写分离**：自动将读操作路由到从库，写操作路由到主库
- **多数据源**：支持配置多个数据库实例并灵活切换
- **连接池集成**：内置 HikariCP 连接池配置
- **事务感知**：在事务中自动使用主库连接
- **自定义连接策略**：支持通过 `JdbcConnectionHint` 控制连接路由
- **遥测集成**：与 FUST 遥测系统集成，提供 SQL 执行监控能力

## 配置方式

### 基础配置

FUST 使用 JSON 格式的配置文件来管理数据库连接。默认配置文件名为 `db-{env}.json`，其中 `{env}` 是当前环境名称（如 dev、integration、testing、staging、production）。

一个基本的数据库配置示例如下：

```json
[
  {
    "name": "example_db",
    "default": true,
    "ds": [
      {
        "type": "master",
        "url": "jdbc:mysql://master.example.com:3306/example_db",
        "username": "user",
        "password": "password"
      },
      {
        "type": "replica",
        "url": "jdbc:mysql://replica.example.com:3306/example_db",
        "username": "user",
        "password": "password"
      }
    ]
  }
]
```

### 完整配置选项

以下是数据库配置的完整选项：

```json
[
  {
    "name": "example_db",
    "default": true,
    "masterOnly": false,
    "useSSL": true,
    "minIdle": 5,
    "maxPoolSize": 20,
    "connectionTimeoutMs": 30000,
    "ds": [
      {
        "type": "master",
        "url": "jdbc:mysql://master.example.com:3306/example_db",
        "username": "user",
        "password": "password"
      },
      {
        "type": "replica",
        "url": "jdbc:mysql://replica1.example.com:3306/example_db",
        "username": "user",
        "password": "password"
      },
      {
        "type": "replica",
        "url": "jdbc:mysql://replica2.example.com:3306/example_db",
        "username": "user",
        "password": "password"
      }
    ]
  }
]
```

#### 数据库实例配置选项：

| 参数 | 说明 | 默认值 |
| ---- | ---- | ------ |
| name | 数据库实例名称，用于区分多个实例 | 必填 |
| default | 是否为默认连接 | false |
| masterOnly | 是否只使用主库，不使用从库 | false |
| useSSL | 是否使用SSL连接 | false |
| minIdle | 连接池中维护的最小空闲连接数 | 10 |
| maxPoolSize | 连接池中允许的最大连接数 | 30 |
| connectionTimeoutMs | 连接超时时间（毫秒） | 30000 |
| ds | 数据源配置列表 | 必填 |

#### 数据源配置选项：

| 参数 | 说明 | 默认值 |
| ---- | ---- | ------ |
| type | 数据源类型，可选值：master（主库）、replica（从库） | master |
| url | JDBC URL | 必填 |
| username | 数据库用户名 | 必填 |
| password | 数据库密码 | 必填 |

### 多数据库配置

FUST 支持配置多个数据库实例，每个实例可以有不同的配置：

```json
[
  {
    "name": "user_db",
    "default": true,
    "ds": [
      {
        "type": "master",
        "url": "jdbc:mysql://master.example.com:3306/user_db",
        "username": "user",
        "password": "password"
      },
      {
        "type": "replica",
        "url": "jdbc:mysql://replica.example.com:3306/user_db",
        "username": "user",
        "password": "password"
      }
    ]
  },
  {
    "name": "product_db",
    "ds": [
      {
        "type": "master",
        "url": "jdbc:mysql://master.example.com:3306/product_db",
        "username": "user",
        "password": "password"
      }
    ]
  }
]
```

在这个配置中，`user_db` 是默认数据库，拥有主库和从库；`product_db` 是第二个数据库，只有主库。

## 自动读写分离

FUST的JDBC组件提供自动读写分离功能，它通过分析SQL语句来确定操作类型：

1. **写操作**：包括INSERT、UPDATE、DELETE、TRUNCATE语句，以及包含"FOR UPDATE"的查询
2. **读操作**：所有不属于写操作的语句，通常是SELECT查询

写操作总是被路由到主库，而读操作则根据配置路由到从库（如果从库可用）。

### SQL识别规则

FUST通过 `SqlUtils` 类实现SQL识别，主要逻辑如下：

```java
private static boolean isUpdateSql(String sql) {
    sql = sql.toLowerCase();
    return sql.startsWith("update") || sql.contains("for update")
           || sql.contains("insert") || sql.contains("delete") || sql.contains("truncate");
}

public static boolean isMaster(String sql) {
    return JdbcConnectionHint.isMaster()
           || TransactionSynchronizationManager.isActualTransactionActive()
           || isUpdateSql(sql);
}
```

此外，FUST还考虑了两个额外因素：

1. **事务状态**：如果当前线程在事务中，所有SQL都会路由到主库
2. **手动指示**：通过 `JdbcConnectionHint` 可以显式指定使用主库或从库

## 使用 JdbcConnectionHint 控制路由

`JdbcConnectionHint` 提供了一种线程安全的方式来手动控制连接路由，它使用ThreadLocal变量存储当前线程的路由信息。

### 基本用法

```java
// 指定使用主库
JdbcConnectionHint.setMaster(true);
try {
    // 执行需要在主库上运行的查询
    return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
} finally {
    // 清除设置
    JdbcConnectionHint.clear();
}

// 指定使用从库
JdbcConnectionHint.setSlave(true);
try {
    // 执行需要在从库上运行的语句
    return jdbcTemplate.update("UPDATE users SET status = 'ACTIVE' WHERE id = ?", userId);
} finally {
    // 清除设置
    JdbcConnectionHint.clear();
}
```

### 多数据库切换

如果配置了多个数据库，可以使用 `setDatabaseName` 方法指定要使用的数据库：

```java
// 切换到指定的数据库
JdbcConnectionHint.setDatabaseName("product_db");
try {
    // 在 product_db 上执行操作
    List<Product> products = jdbcTemplate.query(
        "SELECT * FROM products WHERE category = ?",
        new Object[]{category},
        productRowMapper
    );
    return products;
} finally {
    // 清除设置
    JdbcConnectionHint.clear();
}
```

### 结合主从和数据库选择

可以同时指定数据库和主从节点：

```java
// 切换到product_db数据库的主库
JdbcConnectionHint.setDatabaseName("product_db");
JdbcConnectionHint.setMaster(true);
try {
    // 在product_db的主库上执行操作
    jdbcTemplate.update(
        "INSERT INTO products (name, price) VALUES (?, ?)",
        product.getName(), product.getPrice()
    );
} finally {
    // 清除设置
    JdbcConnectionHint.clear();
}
```

### 提示：务必清除设置

在使用 `JdbcConnectionHint` 后，一定要记得调用 `clear()` 方法清除线程局部变量，否则可能会影响后续的数据库操作。建议使用 try-finally 结构确保清理工作一定会执行。


### 实现自定义连接策略

如果需要更复杂的路由逻辑，可以实现自定义的 `ConnectionStrategy` 接口：

```java
@Component
public class CustomConnectionStrategy implements ConnectionStrategy {
    
    private final JdbcConnectionStrategy defaultStrategy;
    private final List<String> sensitiveTableNames = Arrays.asList(
        "user_accounts", "financial_transactions", "payment_info"
    );
    
    @Autowired
    public CustomConnectionStrategy(JdbcConnectionStrategy defaultStrategy) {
        this.defaultStrategy = defaultStrategy;
    }
    
    @Override
    public Connection getConnection(String sql) throws SQLException {
        // 对包含敏感表的查询强制使用主库
        if (containsSensitiveTable(sql)) {
            return getMasterConnection();
        }
        
        // 其他查询使用默认策略
        return defaultStrategy.getConnection(sql);
    }
    
    @Override
    public Connection getMasterConnection() throws SQLException {
        return defaultStrategy.getMasterConnection();
    }
    
    @Override
    public String getDatabaseName() {
        return defaultStrategy.getDatabaseName();
    }
    
    private boolean containsSensitiveTable(String sql) {
        String lowerSql = sql.toLowerCase();
        return sensitiveTableNames.stream()
            .anyMatch(table -> lowerSql.contains(table.toLowerCase()));
    }
}
```

要使用自定义连接策略，需要将其注册为Spring Bean并确保它替代默认的策略。

## 性能优化建议

### 连接池配置

适当调整连接池配置可以显著提高性能：

```json
{
  "name": "example_db",
  "minIdle": 10,       // 保持足够的空闲连接
  "maxPoolSize": 50,   // 适当增大连接池大小
  "connectionTimeoutMs": 5000,  // 减少连接超时时间
  "ds": [
    // 数据源配置...
  ]
}
```

### 读写分离优化

1. **确保只读操作使用从库**：
   ```java
   JdbcConnectionHint.setSlave(true);
   try {
       // 大量只读查询操作
   } finally {
       JdbcConnectionHint.clear();
   }
   ```

2. **批量操作使用主库**：
   ```java
   JdbcConnectionHint.setMaster(true);
   try {
       jdbcTemplate.batchUpdate(sql, batchArgs);
   } finally {
       JdbcConnectionHint.clear();
   }
   ```

### 避免频繁切换数据源

频繁切换数据源会导致连接池效率降低。尽量在一个业务逻辑块中使用相同的数据源：

```java
// 不推荐
for (Item item : items) {
    JdbcConnectionHint.setDatabaseName("db1");
    processInDb1(item);
    JdbcConnectionHint.clear();
    
    JdbcConnectionHint.setDatabaseName("db2");
    processInDb2(item);
    JdbcConnectionHint.clear();
}

// 推荐
JdbcConnectionHint.setDatabaseName("db1");
try {
    for (Item item : items) {
        processInDb1(item);
    }
} finally {
    JdbcConnectionHint.clear();
}

JdbcConnectionHint.setDatabaseName("db2");
try {
    for (Item item : items) {
        processInDb2(item);
    }
} finally {
    JdbcConnectionHint.clear();
}
```

## 与Spring集成

FUST JDBC组件与Spring的集成非常简单，只需要添加依赖并配置数据源。

### 在Spring Boot应用中使用

```java
@SpringBootApplication
public class Application {
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
```

### 配置备用数据源

```java
@Configuration
public class DataSourceConfig {
    
    @Bean
    @Qualifier("secondaryJdbcTemplate")
    public JdbcTemplate secondaryJdbcTemplate(JdbcConnectionStrategy connectionStrategy) {
        DataSource dataSource = connectionStrategy.getTargetDataSources().get("secondary");
        return new JdbcTemplate(dataSource);
    }
}
```

## 故障排除

### 常见问题

#### 1. 连接总是路由到主库

检查以下几点：
- 是否在事务中执行SQL
- 是否设置了 `JdbcConnectionHint.setMaster(true)` 但忘记清除
- 数据库配置中是否设置了 `"masterOnly": true`

#### 2. 数据库连接超时

可能的原因：
- 连接池大小配置不当
- 数据库服务器负载过高
- 网络问题

解决方案：
- 增加 `connectionTimeoutMs` 值
- 增加 `maxPoolSize` 值
- 检查数据库服务器负载

#### 3. 无法连接到从库

可能的原因：
- 从库配置错误
- 从库服务器不可用

解决方案：
- 验证从库连接信息
- 确保从库服务正常运行
- 临时设置 `"masterOnly": true` 直到从库恢复

## 总结

FUST框架的JDBC支持模块提供了强大的数据库连接管理能力，特别是在主从分离和读写分离方面。通过 `JdbcConnectionHint` 可以灵活控制连接路由，而自定义切面则提供了扩展能力。

主要优势包括：

1. 自动读写分离，减少主库压力
2. 灵活的多数据源支持
3. 与Spring的无缝集成
4. 线程安全的手动路由控制
5. 遥测集成，提供监控能力

通过合理配置和使用这些特性，可以显著提高应用程序的数据库访问性能和稳定性。 