# 数据访问

本章将介绍如何在FUST框架中进行数据库访问配置和操作，包括MySQL数据库配置、实体模型定义、MyBatis的集成以及单元测试。

## 数据库准备

首先，我们需要创建一个MySQL数据库和表结构来存储用户信息。

### 创建数据库

连接到你的MySQL服务器，并执行以下SQL语句创建数据库：

```sql
CREATE DATABASE demo_yoda;
USE demo_yoda;
```

### 创建用户表

执行以下SQL语句创建用户表：

```sql
CREATE TABLE `yd_user` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `birthday` DATE NOT NULL,
    `name` varchar(64) NOT NULL,
    PRIMARY KEY (`id`)
);
```

## 数据库配置

在FUST框架中，数据库的配置支持使用JSON文件的方式，这种方式比传统的Spring Boot配置更加灵活，特别适合多数据源的场景。

### 添加数据库依赖

> fust-boot-mybatis 会自动引入 JDBC 相关依赖

首先，在`demo-yoda-business`模块的`pom.xml`文件中添加以下依赖：


```xml
<!-- FUST MyBatis支持 -->
<dependency>
    <groupId>com.zhihu.fust</groupId>
    <artifactId>fust-boot-mybatis</artifactId>
</dependency>

<!-- 用于测试的H2数据库 -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

### 添加ServiceConfiguration配置

为了让应用能够正确扫描到MyBatis的Mapper接口，需要创建一个配置类并使用`@MapperScan`注解。

在`demo-yoda-business`模块中创建`src/main/java/demo/yoda/business/ServiceConfiguration.java`文件：

```java
package demo.yoda.business;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 服务配置类
 */
@Configuration
@ComponentScan
@MapperScan(annotationClass = Mapper.class)
public class ServiceConfiguration {
}
```

这个配置类有以下几个重要的注解：

- `@Configuration`: 标识这是一个Spring配置类
- `@ComponentScan`: 自动扫描当前包及其子包中的组件
- `@MapperScan(annotationClass = Mapper.class)`: 扫描所有带有`@Mapper`注解的接口，将它们注册为MyBatis的Mapper

### 使用JSON配置数据源

FUST框架使用JSON文件来配置数据源，这样可以更好地管理多环境、多数据库的情况。

在`demo-yoda-business`模块的`src/main/resources`目录下创建`db-dev.json`文件：

```json
[
  {
    "name": "demo_yoda",
    "ds": [
      {
        "name": "demo_yoda-master",
        "url": "jdbc:mysql://127.0.0.1:3306/demo_yoda",
        "username": "root",
        "password": "your_password"
      }
    ]
  }
]
```

这个JSON配置定义了一个名为`demo_yoda`的数据库，包含一个主数据源`demo_yoda-master`。

### 数据源配置说明

JSON配置文件支持以下主要属性：

1. `name`: 数据库的逻辑名称
2. `ds`: 数据源列表，可以配置主从库
3. `default`: 是否为默认数据库（当有多个数据库时）
4. `masterOnly`: 是否只使用主库（忽略从库配置）

数据源配置属性：

1. `name`: 数据源名称，通常格式为`{db-name}-{type}`，type可以是master或replica
2. `url`: JDBC URL
3. `username`: 数据库用户名
4. `password`: 数据库密码
5. `type`: 数据源类型，master（主库）或replica（从库）

### 多环境配置

FUST框架会根据当前环境自动加载对应的数据库配置文件：

- 开发环境：`db-dev.json`
- 测试环境：`db-test.json`
- 生产环境：`db-prod.json`

可以通过系统属性`env.name`或环境变量`ENV_NAME`来指定当前环境。

## 实体模型定义

在FUST框架中，建议将实体模型与数据传输对象分开定义。下面我们创建用户实体模型类。

### 创建UserModel类

在`demo-yoda-business`模块中创建`src/main/java/demo/yoda/business/model/UserModel.java`文件：

```java
package demo.yoda.business.model;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table(name = "yd_user", schema="demo_yoda")
public class UserModel {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDate birthday;
    private String name;
}
```

## 使用MyBatis的TemplateDao

FUST框架提供了`TemplateDao`来简化MyBatis的使用，下面我们将创建`UserDao`类。

### 创建UserDao接口（使用注解方式）

在`demo-yoda-business`模块中创建`src/main/java/demo/yoda/business/dao/UserDao.java`文件，使用MyBatis注解而不是XML映射：

```java
package demo.yoda.business.dao;

import com.zhihu.fust.spring.mybatis.TemplateDao;
import demo.yoda.business.model.UserModel;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserDao extends TemplateDao<UserModel> {
}
```

## 使用TemplateDao的常用方法

FUST框架的`TemplateDao`提供了一组常用的CRUD方法：

```java
// 创建记录
userDao.create(userModel);

// 根据ID查询
UserModel user = userDao.find(1L);

// 更新记录
userDao.update(userModel);

// 根据ID删除
userDao.remove(1L);

// 批量创建
List<UserModel> userList = List.of(user1, user2, user3);
userDao.batchCreate(userList);

// 部分更新（只更新非null字段）
UserModel partialUser = new UserModel();
partialUser.setId(1L);
partialUser.setName("Updated Name");
userDao.patch(partialUser);

// 批量部分更新
List<UserModel> partialUserList = List.of(partialUser1, partialUser2);
userDao.batchPatch(partialUserList);
```

## 使用CollectionDriver进行批量操作

FUST框架支持利用MyBatis的LangDriver机制，简化批量操作，特别是针对集合参数的处理。CollectionDriver主要用于简化批量查询、删除等操作的SQL编写。

### 在DAO接口中使用CollectionDriver

在`UserDao`接口中添加以下方法：

```java
// 表名常量
String TABLE_NAME = "yd_user";

// 使用CollectionDriver处理集合参数
/**
 * 根据ID批量查询用户
 */
@Lang(CollectionDriver.class)
@Select("SELECT * FROM " + TABLE_NAME + " WHERE id IN @ids")
@ResultMap("UserModel")
List<UserModel> findByIds(@Param("ids") Collection<Long> ids);

// 批量删除
@Lang(CollectionDriver.class) 
@Delete("DELETE FROM " + TABLE_NAME + " WHERE id IN @ids") 
int batchDelete(@Param("ids") List<Long> ids);

// 根据名称批量查询
@Lang(CollectionDriver.class)
@Select("SELECT * FROM " + TABLE_NAME + " WHERE name IN @names")
@ResultMap("UserModel")
List<UserModel> findByNames(@Param("names") List<String> names);
```

### 使用CollectionDriver的批量操作方法

使用上述方法可以轻松实现批量操作：

```java
// 批量查询多个用户
List<Long> userIds = List.of(1L, 2L, 3L);
List<UserModel> users = userDao.findByIds(userIds);

// 批量删除
int deletedCount = userDao.batchDelete(List.of(1L, 2L));

// 根据多个名称查询
List<String> names = List.of("User 1", "User 2");
List<UserModel> usersByNames = userDao.findByNames(names);
```

CollectionDriver会自动处理集合参数，将`@ids`和`@names`等占位符转换为适当的SQL语法，从而避免手动拼接SQL字符串。

## 使用H2数据库进行DAO层测试

H2是一个内存数据库，非常适合用于单元测试。

### 配置测试环境

在`demo-yoda-business`模块中创建测试配置类`src/test/java/demo/yoda/business/config/TestBeanConfig.java`：

```java
package demo.yoda.business.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

@Profile("test")
@Configuration
@EnableCaching
public class TestBeanConfig {
    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setName("demo_yoda") // 数据库名
                .setType(EmbeddedDatabaseType.H2) // 内存数据类型: h2 database
                .addScript("classpath:schema-h2.sql") // 加载数据库结构脚本
                .addScript("classpath:data-h2.sql") // 加载初始数据脚本
                .build();
    }
}
```

这种方式使用Java代码直接配置H2数据源，比JSON配置文件更加灵活。配置类包含以下特点：

- `@Profile("test")`：仅在测试环境激活这个配置
- `@EnableCaching`：启用缓存支持
- `dataSource()`方法：创建并配置H2内存数据库
  - 设置数据库名称
  - 指定数据库类型为H2
  - 加载数据库结构和初始数据脚本

### 创建测试用的Schema和数据

在`demo-yoda-business`模块中创建`src/test/resources/schema-h2.sql`文件：

```sql
CREATE TABLE IF NOT EXISTS `yd_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `birthday` DATE NOT NULL,
    `name` VARCHAR(64) NOT NULL,
    PRIMARY KEY (`id`)
);
```

在`demo-yoda-business`模块中创建`src/test/resources/data-h2.sql`文件：

```sql
INSERT INTO `yd_user` (`id`, `birthday`, `name`) VALUES 
(1, '1990-01-01', 'User 1'),
(2, '1992-02-02', 'User 2'),
(3, '1995-03-03', 'User 3');
```

### 编写DAO层单元测试

在`demo-yoda-business`模块中创建`src/test/java/demo/yoda/business/dao/UserDaoTest.java`文件：

```java
package demo.yoda.business.dao;

import com.zhihu.fust.core.dao.PageInfo;
import com.zhihu.fust.core.dao.PageRequest;
import demo.yoda.business.model.UserModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UserDaoTest {
    
    @Autowired
    private UserDao userDao;

    private UserModel createUser(String username) {
        UserModel user = new UserModel();
        user.setBirthday(LocalDate.of(1998, 4, 4));
        user.setName(username);
        userDao.create(user);
        return user;
    }

    @Test
    void testFind() {
        UserModel user = userDao.find(1L);
        assertNotNull(user);
        assertEquals("User 1", user.getName());
    }

    @Test
    void testCreate() {
        UserModel user = new UserModel();
        user.setBirthday(LocalDate.of(1998, 4, 4));
        user.setName("User 4");

        boolean result = userDao.create(user);
        assertTrue(result);
        assertNotNull(user.getId());

        UserModel saved = userDao.find(user.getId());
        assertNotNull(saved);
        assertEquals("User 4", saved.getName());
    }

    @Test
    void testUpdate() {
        UserModel user = createUser("user update");
        assertNotNull(user);

        user.setName("更新的名称");
        boolean result = userDao.update(user);
        assertTrue(result);

        UserModel updated = userDao.find(user.getId());
        assertNotNull(updated);
        assertEquals("更新的名称", updated.getName());
    }

    @Test
    void testRemove() {
        boolean result = userDao.remove(2L);
        assertTrue(result);

        UserModel deleted = userDao.find(2L);
        assertNull(deleted);
    }

    @Test
    void testPatch() {
        UserModel user = createUser("user patch");
        UserModel partialUser = new UserModel();
        partialUser.setId(user.getId());
        partialUser.setName("部分更新名称");

        boolean result = userDao.patch(partialUser);
        assertTrue(result);

        UserModel patched = userDao.find(user.getId());
        assertNotNull(patched);
        assertEquals("部分更新名称", patched.getName());
        // 生日应保持不变
        assertNotNull(patched.getBirthday());
    }

    @Test
    void testFindByIds() {
        // 准备要查询的ID列表
        List<Long> userIds = Arrays.asList(100L, 300L);

        // 调用批量查询方法
        List<UserModel> users = userDao.findByIds(userIds);

        // 验证结果
        assertNotNull(users);
        assertEquals(2, users.size());

    }

}
```

## 总结

在本章中，我们学习了如何在FUST框架中进行数据库访问：

1. 创建MySQL数据库和表结构
2. 使用JSON文件配置数据源
3. 创建实体模型类
4. 使用MyBatis的注解方式定义DAO接口，实现TemplateDao的基本CRUD操作
5. 使用CollectionDriver实现高效的批量查询
6. 利用H2内存数据库进行DAO层测试

通过上述步骤，我们完成了`demo-yoda`项目的数据访问层设计与实现。在下一章中，我们将基于数据访问层构建业务服务层。 