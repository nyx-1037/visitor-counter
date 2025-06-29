package com.nyx.visitorcounter.service;

import com.nyx.visitorcounter.model.User;
import com.nyx.visitorcounter.repository.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * 用户详情服务实现类
 * <p>
 * 实现Spring Security的UserDetailsService接口，
 * 提供根据用户名加载用户详情的功能。
 * 该服务用于认证过程中验证用户身份。
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 根据用户名加载用户详情
     * <p>
     * 从数据库中查找用户，并将其转换为Spring Security的UserDetails对象。
     * 如果用户不存在，则抛出UsernameNotFoundException异常。
     *
     * @param username 要加载的用户的用户名
     * @return 包含用户认证和授权信息的UserDetails对象
     * @throws UsernameNotFoundException 如果指定的用户名不存在
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), new ArrayList<>());
    }
}