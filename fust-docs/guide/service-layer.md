# 服务层开发

本章将介绍如何在FUST框架中开发服务层（Service Layer），构建业务逻辑处理层，以及如何对服务层进行单元测试。我们将以上一章中创建的UserDao为基础，开发UserService实现用户相关的业务逻辑。

## 服务层概述

服务层是应用程序的核心业务逻辑处理层，位于数据访问层（DAO）和表示层（Controller/API）之间。它主要负责：

1. 实现业务规则和流程
2. 封装和组合底层数据访问操作
3. 提供事务管理

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
     * 根据ID列表查询用户
     *
     * @param ids 用户ID列表
     * @return 用户信息列表
     */
    List<UserModel> getUserByIds(List<Long> ids);

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
    public List<UserModel> getUserByIds(List<Long> ids) {
        log.info("Getting user by IDS: {}", ids);
        return userDao.findByIds(ids);
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


## 单元测试UserService

最后，我们需要编写单元测试确保UserService的正确性。

在`demo-yoda-business`模块中创建`src/test/java/demo/yoda/business/service/UserServiceTest.java`文件：

```java
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

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

        assertNull(userService.getUserById(999L));

        verify(userDao, times(1)).find(999L);
    }

} 
```

这个测试类包含以下特点：
1. 对每个服务方法都编写了测试用例
2. 包含异常处理的测试
3. 验证方法的返回值和数据库状态

## 最佳实践

在FUST框架中开发服务层时，请遵循以下最佳实践：

1. **接口分离** - 为服务定义接口，实现接口和实现分离
2. **合理分层** - 服务层专注于业务逻辑，不要包含数据访问或表示层逻辑
3. **事务管理** - 对修改操作使用`@Transactional`注解管理事务
4. **日志记录** - 记录关键业务操作和异常情况
5. **代码测试** - 编写单元测试和集成测试确保代码质量
6. **依赖注入** - 使用构造函数注入依赖，便于测试
7. **命名规范** - 使用清晰的方法命名，表达业务意图

## 总结

在本章中，我们学习了如何在FUST框架中开发服务层：

1. 创建服务接口定义业务方法
2. 实现服务接口，完成业务逻辑处理
3. 添加业务异常处理
4. 编写集成测试和模拟测试

通过上述步骤，我们完成了`demo-yoda`项目的服务层设计与实现。在下一章中，我们将学习如何整合Redis缓存，进一步提升应用性能。 