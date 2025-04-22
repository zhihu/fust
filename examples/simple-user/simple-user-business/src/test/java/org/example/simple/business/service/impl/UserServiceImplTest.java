package org.example.simple.business.service.impl;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import org.example.simple.business.dao.UserDao;
import org.example.simple.business.model.UserModel;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testCreateUser_WhenSuccess_ReturnsTrue() {
        // Arrange
        UserModel user = new UserModel();
        user.setName("测试用户");
        when(userDao.create(any(UserModel.class))).thenReturn(true);

        // Act
        boolean result = userService.createUser(user);

        // Assert
        assertTrue(result);
        verify(userDao, times(1)).create(user);
    }

    @Test
    void testCreateUser_WhenFail_ReturnsFalse() {
        // Arrange
        UserModel user = new UserModel();
        user.setName("测试用户");
        when(userDao.create(any(UserModel.class))).thenReturn(false);

        // Act
        boolean result = userService.createUser(user);

        // Assert
        assertFalse(result);
        verify(userDao, times(1)).create(user);
    }

    @Test
    void testBatchCreateUsers_WhenSuccess_ReturnsTrue() {
        // Arrange
        UserModel user1 = new UserModel();
        user1.setName("用户1");
        UserModel user2 = new UserModel();
        user2.setName("用户2");
        List<UserModel> users = Arrays.asList(user1, user2);
        
        when(userDao.batchCreate(anyList())).thenReturn(true);

        // Act
        boolean result = userService.batchCreateUsers(users);

        // Assert
        assertTrue(result);
        verify(userDao, times(1)).batchCreate(users);
    }

    @Test
    void testBatchCreateUsers_WhenFail_ReturnsFalse() {
        // Arrange
        UserModel user1 = new UserModel();
        user1.setName("用户1");
        UserModel user2 = new UserModel();
        user2.setName("用户2");
        List<UserModel> users = Arrays.asList(user1, user2);
        
        when(userDao.batchCreate(anyList())).thenReturn(false);

        // Act
        boolean result = userService.batchCreateUsers(users);

        // Assert
        assertFalse(result);
        verify(userDao, times(1)).batchCreate(users);
    }

    @Test
    void testGetUserById_WhenExists_ReturnsUser() {
        // Arrange
        Long userId = 1L;
        UserModel mockUser = new UserModel();
        mockUser.setId(userId);
        mockUser.setName("测试用户");
        when(userDao.find(userId)).thenReturn(mockUser);

        // Act
        UserModel result = userService.getUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("测试用户", result.getName());
        verify(userDao, times(1)).find(userId);
    }

    @Test
    void testGetUserById_WhenNotExists_ReturnsNull() {
        // Arrange
        Long userId = 1L;
        when(userDao.find(userId)).thenReturn(null);

        // Act
        UserModel result = userService.getUserById(userId);

        // Assert
        assertNull(result);
        verify(userDao, times(1)).find(userId);
    }

    @Test
    void testGetUsersByIds_WhenExists_ReturnsUsers() {
        // Arrange
        List<Long> userIds = Arrays.asList(1L, 2L);
        UserModel user1 = new UserModel();
        user1.setId(1L);
        user1.setName("用户1");

        UserModel user2 = new UserModel();
        user2.setId(2L);
        user2.setName("用户2");

        List<UserModel> mockUsers = Arrays.asList(user1, user2);
        when(userDao.findByIds(userIds)).thenReturn(mockUsers);

        // Act
        List<UserModel> results = userService.getUserByIds(userIds);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(1L, results.get(0).getId());
        assertEquals("用户1", results.get(0).getName());
        assertEquals(2L, results.get(1).getId());
        assertEquals("用户2", results.get(1).getName());
        verify(userDao, times(1)).findByIds(userIds);
    }

    @Test
    void testGetUsersByIds_WhenEmpty_ReturnsEmptyList() {
        // Arrange
        List<Long> userIds = Arrays.asList(1L, 2L);
        when(userDao.findByIds(userIds)).thenReturn(Arrays.asList());

        // Act
        List<UserModel> results = userService.getUserByIds(userIds);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(userDao, times(1)).findByIds(userIds);
    }

    @Test
    void testUpdateUser_WhenSuccess_ReturnsTrue() {
        // Arrange
        UserModel user = new UserModel();
        user.setId(1L);
        user.setName("更新后的用户");
        when(userDao.update(any(UserModel.class))).thenReturn(true);

        // Act
        boolean result = userService.updateUser(user);

        // Assert
        assertTrue(result);
        verify(userDao, times(1)).update(user);
    }

    @Test
    void testUpdateUser_WhenFail_ReturnsFalse() {
        // Arrange
        UserModel user = new UserModel();
        user.setId(1L);
        user.setName("更新后的用户");
        when(userDao.update(any(UserModel.class))).thenReturn(false);

        // Act
        boolean result = userService.updateUser(user);

        // Assert
        assertFalse(result);
        verify(userDao, times(1)).update(user);
    }

    @Test
    void testPatchUser_WhenSuccess_ReturnsTrue() {
        // Arrange
        UserModel user = new UserModel();
        user.setId(1L);
        user.setName("部分更新用户");
        when(userDao.patch(any(UserModel.class))).thenReturn(true);

        // Act
        boolean result = userService.patchUser(user);

        // Assert
        assertTrue(result);
        verify(userDao, times(1)).patch(user);
    }

    @Test
    void testPatchUser_WhenFail_ReturnsFalse() {
        // Arrange
        UserModel user = new UserModel();
        user.setId(1L);
        user.setName("部分更新用户");
        when(userDao.patch(any(UserModel.class))).thenReturn(false);

        // Act
        boolean result = userService.patchUser(user);

        // Assert
        assertFalse(result);
        verify(userDao, times(1)).patch(user);
    }

    @Test
    void testDeleteUser_WhenSuccess_ReturnsTrue() {
        // Arrange
        Long userId = 1L;
        when(userDao.remove(userId)).thenReturn(true);

        // Act
        boolean result = userService.deleteUser(userId);

        // Assert
        assertTrue(result);
        verify(userDao, times(1)).remove(userId);
    }

    @Test
    void testDeleteUser_WhenFail_ReturnsFalse() {
        // Arrange
        Long userId = 1L;
        when(userDao.remove(userId)).thenReturn(false);

        // Act
        boolean result = userService.deleteUser(userId);

        // Assert
        assertFalse(result);
        verify(userDao, times(1)).remove(userId);
    }

    @Test
    void testBatchDeleteUsers_WhenSuccess_ReturnsCount() {
        // Arrange
        List<Long> userIds = Arrays.asList(1L, 2L);
        when(userDao.batchDelete(userIds)).thenReturn(2);

        // Act
        int result = userService.batchDeleteUsers(userIds);

        // Assert
        assertEquals(2, result);
        verify(userDao, times(1)).batchDelete(userIds);
    }

    @Test
    void testBatchDeleteUsers_WhenPartialSuccess_ReturnsCount() {
        // Arrange
        List<Long> userIds = Arrays.asList(1L, 2L, 3L);
        when(userDao.batchDelete(userIds)).thenReturn(2);

        // Act
        int result = userService.batchDeleteUsers(userIds);

        // Assert
        assertEquals(2, result);
        verify(userDao, times(1)).batchDelete(userIds);
    }

    @Test
    void testBatchDeleteUsers_WhenFail_ReturnsZero() {
        // Arrange
        List<Long> userIds = Arrays.asList(1L, 2L);
        when(userDao.batchDelete(userIds)).thenReturn(0);

        // Act
        int result = userService.batchDeleteUsers(userIds);

        // Assert
        assertEquals(0, result);
        verify(userDao, times(1)).batchDelete(userIds);
    }
} 