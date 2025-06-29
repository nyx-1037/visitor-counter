package com.nyx.visitorcounter.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.nyx.visitorcounter.model.PageResult;
import com.nyx.visitorcounter.model.User;
import com.nyx.visitorcounter.repository.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务
 * 提供用户注册、查询、创建、更新、删除等功能
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 注册新用户
     * 对用户密码进行加密处理后保存到数据库
     * 
     * @param user 要注册的用户对象
     * @return 注册后的用户对象
     */
    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
        return user;
    }

    /**
     * 根据用户名查找用户
     * 
     * @param username 用户名
     * @return 用户对象，如果不存在则返回null
     */
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    /**
     * 获取所有用户
     * 
     * @return 所有用户列表
     */
    public java.util.List<User> getAllUsers() {
        return userMapper.selectList(null);
    }
    
    /**
     * 分页查询用户列表，支持模糊查询
     * @param pageNum 页码
     * @param pageSize 每页记录数
     * @param username 用户名（模糊查询）
     * @param status 状态
     * @return 分页结果
     */
    public PageResult<User> getUsersByPage(int pageNum, int pageSize, String username, Integer status) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> users = userMapper.findUsersByPage(username, status);
        PageInfo<User> pageInfo = new PageInfo<>(users);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getPages());
    }

    /**
     * 删除指定ID的用户
     * 
     * @param id 要删除的用户ID
     */
    public void deleteUser(Integer id) {
        userMapper.deleteById(id);
    }

    /**
     * 更新用户信息
     * 如果包含密码字段，则对密码进行加密处理
     * 
     * @param user 要更新的用户对象
     * @return 更新后的用户对象
     */
    public User updateUser(User user) {
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userMapper.updateById(user);
        return user;
    }

    /**
     * 更新用户状态
     * 
     * @param id 要更新状态的用户ID
     * @param status 新的状态值
     */
    public void updateUserStatus(Integer id, Integer status) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setStatus(status);
            userMapper.updateById(user);
        }
    }
}