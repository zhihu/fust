# 服务层开发

本章将介绍如何在FUST框架中开发服务层（Service Layer），构建业务逻辑处理层，以及如何对服务层进行单元测试。我们将以上一章中创建的UserDao为基础，开发UserService实现用户相关的业务逻辑。

## 服务层概述

服务层是应用程序的核心业务逻辑处理层，位于数据访问层（DAO）和表示层（Controller/API）之间。它主要负责：

1. 实现业务规则和流程
2. 封装和组合底层数据访问操作
3. 提供事务管理
4. 实现安全验证和业务校验
5. 处理异常并转换为业务异常

在FUST框架中，服务层通常使用Spring的`@Service`注解标记，并通过依赖注入获取所需的DAO组件。

## 创建UserService接口

首先，我们为UserService创建一个接口，定义用户管理的核心业务操作。

在`demo-yoda-business`模块中创建`src/main/java/demo/yoda/business/service/UserService.java`文件：

```java
package demo.yoda.business.service;

import demo.yoda.business.model.UserModel;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 创建用户
     * 
     * @param user 用户信息
     * @return 创建成功返回true，否则返回false
     */
    boolean createUser(UserModel user);
    
    /**
     * 批量创建用户
     * 
     * @param users 用户列表
     * @return 创建成功返回true，否则返回false
     */
    boolean batchCreateUsers(List<UserModel> users);
    
    /**
     * 根据ID查询用户
     * 
     * @param id 用户ID
     * @return 用户信息
     */
    UserModel getUserById(Long id);
    
    /**
     * 根据名称查询用户
     * 
     * @param name 用户名称
     * @return 用户信息
     */
    Optional<UserModel> getUserByName(String name);
    
    /**
     * 根据生日查询用户
     * 
     * @param birthday 生日
     * @return 用户列表
     */
    List<UserModel> getUsersByBirthday(LocalDate birthday);
    
    /**
     * 更新用户信息
     * 
     * @param user 用户信息
     * @return 更新成功返回true，否则返回false
     */
    boolean updateUser(UserModel user);
    
    /**
     * 部分更新用户信息（只更新非null字段）
     * 
     * @param user 用户信息
     * @return 更新成功返回true，否则返回false
     */
    boolean patchUser(UserModel user);
    
    /**
     * 删除用户
     * 
     * @param id 用户ID
     * @return 删除成功返回true，否则返回false
     */
    boolean deleteUser(Long id);
    
    /**
     * 批量删除用户
     * 
     * @param ids 用户ID列表
     * @return 删除的用户数量
     */
    int batchDeleteUsers(List<Long> ids);
}
```

## 实现UserService

接下来，我们实现UserService接口，完成业务逻辑处理。

在`demo-yoda-business`模块中创建`src/main/java/demo/yoda/business/service/impl/UserServiceImpl.java`文件：

```java
package demo.yoda.business.service.impl;

import demo.yoda.business.dao.UserDao;
import demo.yoda.business.model.UserModel;
import demo.yoda.business.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 用户服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserDao userDao;
    
    @Override
    @Transactional
    public boolean createUser(UserModel user) {
        log.info("Creating user: {}", user.getName());
        return userDao.create(user);
    }
    
    @Override
    @Transactional
    public boolean batchCreateUsers(List<UserModel> users) {
        log.info("Batch creating {} users", users.size());
        return userDao.batchCreate(users);
    }
    
    @Override
    public UserModel getUserById(Long id) {
        log.info("Getting user by ID: {}", id);
        return userDao.find(id);
    }
    
    @Override
    public Optional<UserModel> getUserByName(String name) {
        log.info("Getting user by name: {}", name);
        return userDao.findByName(name);
    }
    
    @Override
    public List<UserModel> getUsersByBirthday(LocalDate birthday) {
        log.info("Getting users by birthday: {}", birthday);
        return userDao.findByBirthday(birthday.toString());
    }
    
    @Override
    @Transactional
    public boolean updateUser(UserModel user) {
        log.info("Updating user: {}", user.getId());
        return userDao.update(user);
    }
    
    @Override
    @Transactional
    public boolean patchUser(UserModel user) {
        log.info("Patching user: {}", user.getId());
        return userDao.patch(user);
    }
    
    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        log.info("Deleting user: {}", id);
        return userDao.remove(id);
    }
    
    @Override
    @Transactional
    public int batchDeleteUsers(List<Long> ids) {
        log.info("Batch deleting users: {}", ids);
        return userDao.batchDelete(ids);
    }
}
```

这个实现类有以下几个特点：

1. 使用`@Service`注解标记为Spring服务bean
2. 使用`@Slf4j`注解自动创建日志记录器
3. 使用`@RequiredArgsConstructor`自动生成构造函数，实现依赖注入
4. 使用`@Transactional`管理事务，确保数据一致性
5. 实现了所有业务方法，并添加了日志记录

## 业务异常处理

在服务层中处理业务异常是一个良好的实践。我们创建一个用户相关的业务异常类。

在`demo-yoda-business`模块中创建`src/main/java/demo/yoda/business/exception/UserNotFoundException.java`文件：

```java
package demo.yoda.business.exception;

/**
 * 用户不存在异常
 */
public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(Long id) {
        super("User not found with id: " + id);
    }
    
    public UserNotFoundException(String name) {
        super("User not found with name: " + name);
    }
}
```

然后，我们修改UserServiceImpl类中的方法，添加异常处理：

```java
@Override
public UserModel getUserById(Long id) {
    log.info("Getting user by ID: {}", id);
    UserModel user = userDao.find(id);
    if (user == null) {
        throw new UserNotFoundException(id);
    }
    return user;
}

@Override
public Optional<UserModel> getUserByName(String name) {
    log.info("Getting user by name: {}", name);
    Optional<UserModel> user = userDao.findByName(name);
    if (user.isEmpty()) {
        log.warn("User not found with name: {}", name);
    }
    return user;
}
```

## 单元测试UserService

最后，我们需要编写单元测试确保UserService的正确性。

在`demo-yoda-business`模块中创建`src/test/java/demo/yoda/business/service/UserServiceTest.java`文件：

```java
package demo.yoda.business.service;

import demo.yoda.business.config.TestBeanConfig;
import demo.yoda.business.dao.UserDao;
import demo.yoda.business.exception.UserNotFoundException;
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
@Transactional
class UserServiceTest {
    
    @Autowired
    private UserService userService;
    
    @Test
    void testCreateUser() {
        UserModel user = new UserModel();
        user.setBirthday(LocalDate.of(1998, 4, 4));
        user.setName("User 4");
        
        boolean result = userService.createUser(user);
        assertTrue(result);
        assertNotNull(user.getId());
        
        UserModel saved = userService.getUserById(user.getId());
        assertNotNull(saved);
        assertEquals("User 4", saved.getName());
    }
    
    @Test
    void testGetUserById() {
        UserModel user = userService.getUserById(1L);
        assertNotNull(user);
        assertEquals("User 1", user.getName());
    }
    
    @Test
    void testGetUserById_NotFound() {
        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(999L);
        });
    }
    
    @Test
    void testGetUserByName() {
        Optional<UserModel> user = userService.getUserByName("User 2");
        assertTrue(user.isPresent());
        assertEquals(2L, user.get().getId());
    }
    
    @Test
    void testGetUserByName_NotFound() {
        Optional<UserModel> user = userService.getUserByName("Nonexistent User");
        assertTrue(user.isEmpty());
    }
    
    @Test
    void testGetUsersByBirthday() {
        List<UserModel> users = userService.getUsersByBirthday(LocalDate.of(1990, 1, 1));
        assertEquals(1, users.size());
        assertEquals("User 1", users.get(0).getName());
    }
    
    @Test
    void testUpdateUser() {
        UserModel user = userService.getUserById(3L);
        assertNotNull(user);
        
        user.setName("Updated Name");
        boolean result = userService.updateUser(user);
        assertTrue(result);
        
        UserModel updated = userService.getUserById(3L);
        assertNotNull(updated);
        assertEquals("Updated Name", updated.getName());
    }
    
    @Test
    void testPatchUser() {
        UserModel partialUser = new UserModel();
        partialUser.setId(1L);
        partialUser.setName("Patched Name");
        
        boolean result = userService.patchUser(partialUser);
        assertTrue(result);
        
        UserModel patched = userService.getUserById(1L);
        assertNotNull(patched);
        assertEquals("Patched Name", patched.getName());
        // Birthday should remain unchanged
        assertNotNull(patched.getBirthday());
    }
    
    @Test
    void testDeleteUser() {
        boolean result = userService.deleteUser(2L);
        assertTrue(result);
        
        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(2L);
        });
    }
    
    @Test
    void testBatchDeleteUsers() {
        int deleted = userService.batchDeleteUsers(List.of(1L, 3L));
        assertEquals(2, deleted);
        
        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(1L);
        });
        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(3L);
        });
    }
}
```

这个测试类包含以下特点：

1. 使用`@SpringBootTest`注解启动完整的应用上下文
2. 使用`@ActiveProfiles("test")`激活测试配置
3. 使用`@Transactional`确保测试方法间的数据隔离
4. 对每个服务方法都编写了测试用例
5. 包含异常处理的测试
6. 验证方法的返回值和数据库状态

## 使用Mockito进行模拟测试

除了集成测试外，我们还可以使用Mockito进行模拟测试，这样可以隔离依赖，专注于服务层逻辑的测试。

在`demo-yoda-business`模块中创建`src/test/java/demo/yoda/business/service/UserServiceMockTest.java`文件：

```java
package demo.yoda.business.service;

import demo.yoda.business.dao.UserDao;
import demo.yoda.business.exception.UserNotFoundException;
import demo.yoda.business.model.UserModel;
import demo.yoda.business.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceMockTest {
    
    @Mock
    private UserDao userDao;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    private UserModel testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new UserModel();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
    }
    
    @Test
    void testGetUserById() {
        when(userDao.find(1L)).thenReturn(testUser);
        
        UserModel result = userService.getUserById(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test User", result.getName());
        verify(userDao, times(1)).find(1L);
    }
    
    @Test
    void testGetUserById_NotFound() {
        when(userDao.find(999L)).thenReturn(null);
        
        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(999L);
        });
        
        verify(userDao, times(1)).find(999L);
    }
    
    @Test
    void testGetUserByName() {
        when(userDao.findByName("Test User")).thenReturn(Optional.of(testUser));
        
        Optional<UserModel> result = userService.getUserByName("Test User");
        
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(userDao, times(1)).findByName("Test User");
    }
    
    @Test
    void testCreateUser() {
        when(userDao.create(testUser)).thenReturn(true);
        
        boolean result = userService.createUser(testUser);
        
        assertTrue(result);
        verify(userDao, times(1)).create(testUser);
    }
}
```

Mockito测试的优点：

1. 测试更加专注于服务层逻辑
2. 执行速度更快，不需要数据库操作
3. 可以精确控制依赖组件的行为
4. 便于测试边界条件和异常情况

## 最佳实践

在FUST框架中开发服务层时，请遵循以下最佳实践：

1. **接口分离** - 为服务定义接口，实现接口和实现分离
2. **合理分层** - 服务层专注于业务逻辑，不要包含数据访问或表示层逻辑
3. **事务管理** - 对修改操作使用`@Transactional`注解管理事务
4. **异常处理** - 捕获底层异常并转换为有意义的业务异常
5. **日志记录** - 记录关键业务操作和异常情况
6. **代码测试** - 编写单元测试和集成测试确保代码质量
7. **依赖注入** - 使用构造函数注入依赖，便于测试
8. **命名规范** - 使用清晰的方法命名，表达业务意图

## 总结

在本章中，我们学习了如何在FUST框架中开发服务层：

1. 创建服务接口定义业务方法
2. 实现服务接口，完成业务逻辑处理
3. 添加业务异常处理
4. 编写集成测试和模拟测试

通过上述步骤，我们完成了`demo-yoda`项目的服务层设计与实现。在下一章中，我们将学习如何整合Redis缓存，进一步提升应用性能。 