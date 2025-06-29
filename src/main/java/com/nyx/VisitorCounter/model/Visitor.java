package com.nyx.visitorcounter.model;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 访问量统计实体类
 * 用于记录不同目标页面的访问统计信息
 */

/**
 * 使用Lombok的@Data注解自动生成getter、setter、equals、hashCode和toString方法
 */
@Data
/**
 * 指定对应的数据库表名为visitor_tb
 */
@TableName("visitor_tb")
public class Visitor {
    /**
     * 访问量记录ID，主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 目标页面标识，通常是URL路径或页面唯一标识符
     */
    @TableField("target")
    private String target;

    /**
     * 访问计数，默认为0
     */
    @TableField("count")
    private Long count = 0L;

    /**
     * 目标页面描述信息
     */
    @TableField("description")
    private String description;

    /**
     * 状态：1-启用，0-禁用
     * 默认为启用状态(1)
     */
    @TableField("status")
    private Integer status = 1;
}