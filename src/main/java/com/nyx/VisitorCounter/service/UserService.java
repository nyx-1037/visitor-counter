package com.nyx.visitorcounter.service;

import com.nyx.visitorcounter.model.User;
import com.nyx.visitorcounter.repository.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;



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