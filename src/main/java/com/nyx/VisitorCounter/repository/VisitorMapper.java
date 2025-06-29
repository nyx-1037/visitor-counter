package com.nyx.visitorcounter.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nyx.visitorcounter.model.Visitor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VisitorMapper extends BaseMapper<Visitor> {
    @Select("SELECT * FROM visitor_tb WHERE target = #{target}")
    Visitor findByTarget(String target);
}