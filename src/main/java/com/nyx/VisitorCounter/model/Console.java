package com.nyx.visitorcounter.model;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 访问日志实体类
 * 用于记录每次访问的详细信息，包括IP地址和访问时间
 */

/**
 * 使用Lombok的@Data注解自动生成getter、setter、equals、hashCode和toString方法
 */
@Data
/**
 * 指定对应的数据库表名为console_tb
 */
@TableName("console_tb")
public class Console {
    /**
     * 访问日志ID，主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 关联的访问量记录ID，外键关联visitor_tb表
     */
    @TableField("visitor_id")
    private Integer visitorId;

    /**
     * 访问者的IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 访问创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    // Getter and Setter methods
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getVisitorId() {
        return visitorId;
    }

    public void setVisitorId(Integer visitorId) {
        this.visitorId = visitorId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}