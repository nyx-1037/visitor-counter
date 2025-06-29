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

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
        return user;
    }

    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

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

    public void deleteUser(Integer id) {
        userMapper.deleteById(id);
    }

    public User updateUser(User user) {
        userMapper.updateById(user);
        return user;
    }

    public void updateUserStatus(Integer id, Integer status) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setStatus(status);
            userMapper.updateById(user);
        }
    }
}