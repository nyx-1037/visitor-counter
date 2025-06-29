package com.nyx.visitorcounter.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nyx.visitorcounter.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT * FROM user_tb WHERE username = #{username}")
    User findByUsername(String username);
    
    /**
     * 分页查询用户列表，支持模糊查询
     * @param username 用户名（模糊查询）
     * @param status 状态
     * @return 用户列表
     */
    List<User> findUsersByPage(@Param("username") String username, @Param("status") Integer status);
}