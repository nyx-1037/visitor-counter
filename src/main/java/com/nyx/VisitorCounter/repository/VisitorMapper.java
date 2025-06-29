package com.nyx.visitorcounter.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nyx.visitorcounter.model.Visitor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VisitorMapper extends BaseMapper<Visitor> {
    @Select("SELECT * FROM visitor_tb WHERE target = #{target}")
    Visitor findByTarget(String target);
    
    /**
     * 分页查询访问量列表，支持模糊查询
     * @param target 目标（模糊查询）
     * @param description 描述（模糊查询）
     * @param status 状态
     * @return 访问量列表
     */
    List<Visitor> findVisitorsByPage(@Param("target") String target, 
                                    @Param("description") String description, 
                                    @Param("status") Integer status);
}