package org.example.simple.business.dao;

import org.example.simple.business.TestConfiguration;
import org.example.simple.business.model.UserModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestConfiguration
@ExtendWith(SpringExtension.class)
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
        UserModel user = userDao.find(100L);
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
        boolean result = userDao.remove(200L);
        assertTrue(result);

        UserModel deleted = userDao.find(200L);
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