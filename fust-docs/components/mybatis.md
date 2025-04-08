# MyBatis 增强

FUST 框架提供了对 MyBatis 的增强支持，简化了数据库访问层的开发，提供了丰富的扩展功能。本文档将介绍如何在 FUST 应用中配置和使用 MyBatis 增强特性。

## 特性概览

- 多数据源配置支持
- 基于 TemplateDao 的通用 CRUD 操作
- 自动生成表元数据
- 批量操作支持
- 自定义类型处理器
- 自定义 SQL 语言驱动
- 数据库字段自动填充

## 数据源配置

### 基础配置

FUST 使用 JSON 格式的配置文件来管理数据库连接。默认配置文件名为 `db-{env}.json`，其中 `{env}` 是当前环境名称（如 dev、integration、testing、staging、production）。

一个基本的数据库配置示例如下：

```json
[
  {
    "name": "db1",
    "ds": [
      {
        "name": "db1-master",
        "url": "jdbc:mysql://127.0.0.1:3306/db1",
        "username": "root",
        "password": "123"
      }
    ]
  }
]
```

这个配置创建了一个名为 "db1" 的数据库连接，连接到本地的 MySQL 服务器。

### 多数据源配置

FUST 支持配置多个数据源，每个数据源可以有不同的配置：

```json
[
  {
    "name": "db1",
    "defaultDatabase": true,
    "ds": [
      {
        "name": "db1-master",
        "url": "jdbc:mysql://127.0.0.1:3306/db1",
        "username": "root",
        "password": "123"
      }
    ]
  },
  {
    "name": "db2",
    "ds": [
      {
        "name": "db2-master",
        "url": "jdbc:mysql://127.0.0.1:3306/db2",
        "username": "root",
        "password": "123"
      }
    ]
  }
]
```

在多数据源配置中，可以通过 `defaultDatabase` 参数指定默认数据源。如果不指定，则第一个数据源会被设置为默认数据源。

### 读写分离配置

对于需要读写分离的场景，可以配置主从数据源：

```json
[
  {
    "name": "db1",
    "ds": [
      {
        "name": "db1-master",
        "url": "jdbc:mysql://master.example.com:3306/db1",
        "username": "root",
        "password": "123",
        "type": "master"
      },
      {
        "name": "db1-slave1",
        "url": "jdbc:mysql://slave1.example.com:3306/db1",
        "username": "readonly",
        "password": "123",
        "type": "slave"
      },
      {
        "name": "db1-slave2",
        "url": "jdbc:mysql://slave2.example.com:3306/db1",
        "username": "readonly",
        "password": "123",
        "type": "slave"
      }
    ]
  }
]
```

通过设置 `type` 参数为 "master" 或 "slave"，可以指定数据源的类型。

### 完整配置选项

以下是数据源配置的完整选项：

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
        "password": "123",
        "type": "master",
        "driverClassName": "com.mysql.cj.jdbc.Driver",
        "connectionTimeout": 30000,
        "idleTimeout": 600000,
        "maxLifetime": 1800000,
        "maximumPoolSize": 10,
        "minimumIdle": 5,
        "connectionTestQuery": "SELECT 1",
        "autoCommit": true,
        "poolName": "HikariPool-DB1",
        "registerMbeans": false
      }
    ]
  }
]
```

这些配置参数会被直接传递给 HikariCP 连接池。

## 实体类定义

在 FUST 框架中，实体类使用 JPA 注解来定义表结构和字段映射：

```java
@Table(name = "yd_user")
@Getter
@Setter
public class UserModel {
    @Id
    private long id;
    private LocalDate birthday;
    private String name;

    @DbAutoColumn
    private Instant createdAt;
    @DbAutoColumn
    private Instant updatedAt;
}
```

### 主要注解

- `@Table`: 指定实体类对应的表名
- `@Id`: 标记主键字段
- `@DbAutoColumn`: 标记由数据库自动填充的字段，例如 created_at, updated_at 等

## TemplateDao 通用操作

FUST 提供了 `TemplateDao` 接口，封装了常见的 CRUD 操作，减少重复代码：

```java
@Mapper
public interface UserDao extends TemplateDao<UserModel> {
}
```

通过继承 `TemplateDao`，你的 DAO 接口自动获得以下方法：

```java
// 创建记录
boolean create(T model);

// 根据ID查询
T find(Serializable id);

// 更新记录
boolean update(T model);

// 删除记录
boolean remove(Serializable id);

// 批量创建
boolean batchCreate(List<T> models);

// 部分更新（忽略 null 字段）
boolean patch(T model);

// 批量部分更新
boolean batchPatch(List<T> models);
```

### 使用示例

```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserDao userDao;
    
    public UserModel getUserById(long id) {
        return userDao.find(id);
    }
    
    public boolean createUser(UserModel user) {
        return userDao.create(user);
    }
    
    public boolean updateUserName(long id, String newName) {
        UserModel user = new UserModel();
        user.setId(id);
        user.setName(newName);
        return userDao.patch(user);
    }
    
    public boolean batchCreateUsers(List<UserModel> users) {
        return userDao.batchCreate(users);
    }
}
```

## 自定义 SQL 查询

除了使用 `TemplateDao` 提供的通用方法外，你还可以定义自定义的查询方法：

```java
@Mapper
public interface UserDao extends TemplateDao<UserModel> {
    @Select("SELECT * FROM yd_user WHERE name = #{name}")
    UserModel findByName(String name);
    
    @Select("SELECT * FROM yd_user WHERE birthday > #{startDate} AND birthday < #{endDate}")
    List<UserModel> findByBirthdayRange(@Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate);
}
```

## 集合参数简化 - CollectionDriver

FUST 提供了 `CollectionDriver` 语言驱动，简化了集合参数的使用：

```java
@LangDriver(CollectionDriver.class)
@Select("SELECT * FROM yd_user WHERE id in @ids")
List<UserModel> findByIds(List<Long> ids);
```

使用 `@ids` 语法可以直接将集合参数传递给 SQL 查询，无需手动处理。这等同于以下 MyBatis 代码：

```java
@Select("<script>SELECT * FROM yd_user WHERE id in <foreach collection='ids' item='__item' separator=','>#{__item}</foreach></script>")
List<UserModel> findByIds(List<Long> ids);
```

### CollectionDriver 原理

`CollectionDriver` 通过正则表达式替换 `@paramName` 语法为 MyBatis 的 `<foreach>` 标签，简化了批量操作的编写：

```java
public class CollectionDriver extends XMLLanguageDriver implements LanguageDriver {
    private static final Pattern inPattern = Pattern.compile("@(\\w+)");
    private static final String FOR_EACH = "(<foreach collection=\"$1\" item=\"__item\" "
            + "separator=\",\" >#{__item}</foreach>)";

    @Override
    public SqlSource createSqlSource(Configuration configuration,
                                     String script, Class<?> parameterType) {
        Matcher matcher = inPattern.matcher(script);
        if (matcher.find()) {
            script = matcher.replaceAll(FOR_EACH);
        }

        script = "<script>" + script + "</script>";
        return super.createSqlSource(configuration, script, parameterType);
    }
}
```

## 自动生成字段处理

使用 `@DbAutoColumn` 注解可以标记由数据库自动填充的字段，如创建时间、更新时间等：

```java
@Table(name = "yd_user")
public class UserModel {
    @Id
    private long id;
    
    private String name;
    
    @DbAutoColumn
    private Instant createdAt;
    
    @DbAutoColumn
    private Instant updatedAt;
}
```

添加了 `@DbAutoColumn` 注解的字段在执行 insert 或 update 操作时会被忽略，由数据库的默认值或触发器自动填充。

## 最佳实践

### 实体类设计

1. **使用包装类型**：使用 `Long` 而不是 `long`，使用 `Integer` 而不是 `int`，这样可以区分字段值是否为 null
2. **统一命名风格**：实体类属性使用驼峰命名，数据库字段使用下划线命名
3. **使用合适的注解**：合理使用 `@Id`、`@DbAutoColumn` 等注解

### 数据库操作

1. **使用 TemplateDao**：优先使用 `TemplateDao` 提供的通用方法，避免重复编写 CRUD 操作
2. **批量操作**：对于批量操作，使用 `batchCreate` 和 `batchPatch` 方法
3. **使用 patch 更新**：对于部分字段更新，优先使用 `patch` 方法，而不是先查询再更新

### 连接池配置

1. **合理设置连接池参数**：根据应用负载合理设置最大连接数、最小空闲连接数等参数
2. **配置读写分离**：对于读多写少的场景，配置读写分离可以提高性能

## 故障排查

### 常见问题

1. **连接超时**：检查数据库服务器是否可达，可能需要调整 `connectionTimeout` 参数
2. **连接池容量不足**：对于高并发场景，可能需要增加 `maximumPoolSize` 参数
3. **SQL 语法错误**：检查 SQL 语句是否正确，特别是自定义查询

### 诊断方法

可以通过以下方法诊断数据库连接问题：

1. 检查应用日志中的数据库相关错误
2. 使用数据库客户端工具直接连接数据库，验证连通性
3. 开启 MyBatis 的详细日志，查看执行的 SQL 语句：

```properties
logging.level.org.mybatis=DEBUG
logging.level.com.zhihu.fust.spring.mybatis=DEBUG
```

## 进阶功能

### 自定义类型处理器

通过注解可以为字段指定自定义的类型处理器：

```java
@Table(name = "yd_user")
public class UserModel {
    @Id
    private long id;
    
    @ColumnTypeHandler(JsonTypeHandler.class)
    private UserPreference preference;
}
```

### 手动 ID 生成

默认情况下，实体类的 ID 由数据库自动生成。如果需要手动指定 ID，可以使用 `@ManualId` 注解：

```java
@Table(name = "yd_user")
public class UserModel {
    @Id
    @ManualId
    private long id;
    
    private String name;
}
```

添加了 `@ManualId` 注解的 ID 字段在插入操作时会保留用户设置的值。
