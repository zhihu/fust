package org.example.simple.business.service.impl;

import org.example.simple.business.TestConfiguration;
import org.example.simple.business.model.UserModel;
import org.example.simple.business.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestConfiguration
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
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
        user.setName("Cache Test User");
        user.setBirthday(LocalDate.now());
        boolean created = userService.createUser(user);
        assertTrue(created);

        // 第一次查询，应该从数据库加载并缓存
        Optional<UserModel> result1 = userService.findByCache(user.getId());
        assertTrue(result1.isPresent());

        // 验证缓存是否存在
        String key = "user:" + user.getId();
        assertEquals(Boolean.TRUE, redisTemplate.hasKey(key));

        // 第二次查询，应该从缓存加载
        Optional<UserModel> result2 = userService.findByCache(user.getId());
        assertTrue(result2.isPresent());

        // 更新用户，缓存应该被清除
        user.setName("Updated Cache User");
        userService.updateUser(user);
        assertNotEquals(Boolean.TRUE, redisTemplate.hasKey(key));

        // 再次查询，应该重新缓存
        Optional<UserModel> result3 = userService.findByCache(user.getId());
        assertTrue(result3.isPresent());
        assertEquals("Updated Cache User", result3.get().getName());

        // 删除用户，缓存应该被清除
        userService.deleteUser(user.getId());
        assertNotEquals(Boolean.TRUE, redisTemplate.hasKey(key));
    }

    @Test
    @Transactional
    void testPatchUserClearCache() {
        // 创建一个新用户
        UserModel user = new UserModel();
        user.setName("Patch Test User");
        boolean created = userService.createUser(user);
        assertTrue(created);

        // 查询并缓存
        userService.findByCache(user.getId());

        // 验证缓存是否存在
        String key = "user:" + user.getId();
        assertTrue(redisTemplate.hasKey(key));

        // 部分更新用户，缓存应该被清除
        UserModel patchedUser = new UserModel();
        patchedUser.setId(user.getId());
        patchedUser.setName("Patched User");
        userService.patchUser(patchedUser);

        assertFalse(redisTemplate.hasKey(key));
    }

    @Test
    @Transactional
    void testBatchDeleteUsersClearCache() {
        // 创建几个用户
        UserModel user1 = new UserModel();
        user1.setName("Batch User 1");
        userService.createUser(user1);

        UserModel user2 = new UserModel();
        user2.setName("Batch User 2");
        userService.createUser(user2);

        // 查询并缓存
        userService.findByCache(user1.getId());
        userService.findByCache(user2.getId());

        // 验证缓存是否存在
        String key1 = "user:" + user1.getId();
        String key2 = "user:" + user2.getId();
        assertTrue(redisTemplate.hasKey(key1));
        assertTrue(redisTemplate.hasKey(key2));

        // 批量删除用户，缓存应该被清除
        userService.batchDeleteUsers(java.util.Arrays.asList(user1.getId(), user2.getId()));

        assertFalse(redisTemplate.hasKey(key1));
        assertFalse(redisTemplate.hasKey(key2));
    }
} 