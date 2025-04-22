package org.example.simple.business.service;

import java.util.List;
import java.util.Optional;

import org.example.simple.business.model.UserModel;

/**
 * 用户服务接口
 */

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
    
    /**
     * 从缓存中获取用户，如果不存在则从数据库加载并缓存
     *
     * @param id 用户ID
     * @return 用户信息
     */
    Optional<UserModel> findByCache(Long id);
}