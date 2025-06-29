package com.nyx.visitorcounter.model;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 用户实体类
 * 用于存储系统用户信息，包括用户名、密码和状态等
 */
@Data
@TableName("user_tb")
public class User {
    /**
     * 用户ID，主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户名，用于登录系统
     */
    @TableField("username")
    private String username;

    /**
     * 用户密码，存储加密后的密码
     */
    @TableField("password")
    private String password;

    /**
     * 用户状态
     * 1: 正常
     * 0: 禁用
     */
    @TableField("status")
    private Integer status = 1;
}