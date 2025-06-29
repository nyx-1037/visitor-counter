package com.nyx.visitorcounter.model;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("visitor_tb")
public class Visitor {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("target")
    private String target;

    @TableField("count")
    private Long count = 0L;

    @TableField("description")
    private String description;

    @TableField("status")
    private Integer status = 1;
}