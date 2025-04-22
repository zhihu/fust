package org.example.simple.business.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.simple.business.dao.UserDao;
import org.example.simple.business.model.UserModel;
import org.example.simple.business.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String USER_CACHE_KEY_PREFIX = "user:";
    private static final long USER_CACHE_EXPIRE_TIME = 30; // 30分钟

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
    public boolean patchUser(UserModel user) {
        log.info("Patching user: {}", user.getId());
        boolean result = userDao.patch(user);
        if (result) {
            String key = USER_CACHE_KEY_PREFIX + user.getId();
            redisTemplate.delete(key);
            log.info("Deleted user cache after patch: {}", user.getId());
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

    @Override
    @Transactional
    public int batchDeleteUsers(List<Long> ids) {
        log.info("Batch deleting users: {}", ids);
        int count = userDao.batchDelete(ids);
        
        if (count > 0) {
            for (Long id : ids) {
                String key = USER_CACHE_KEY_PREFIX + id;
                redisTemplate.delete(key);
            }
            log.info("Deleted cache for {} users after batch deletion", count);
        }
        
        return count;
    }
    
    @Override
    public Optional<UserModel> findByCache(Long id) {
        String key = USER_CACHE_KEY_PREFIX + id;
        String json = redisTemplate.opsForValue().get(key);
        
        if (json != null) {
            try {
                log.info("Found user in cache: {}", id);
                return Optional.of(objectMapper.readValue(json, UserModel.class));
            } catch (Exception e) {
                log.error("Failed to deserialize user from cache: {}", id, e);
                // 缓存数据有问题，删除缓存
                redisTemplate.delete(key);
            }
        }
        
        // 缓存不存在或反序列化失败，从数据库查询
        UserModel user = userDao.find(id);
        if (user != null) {
            try {
                String userJson = objectMapper.writeValueAsString(user);
                redisTemplate.opsForValue().set(key, userJson, USER_CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
                log.info("Cached user: {}", id);
                return Optional.of(user);
            } catch (Exception e) {
                log.error("Failed to serialize user to cache: {}", id, e);
            }
            return Optional.of(user);
        }
        
        return Optional.empty();
    }
}