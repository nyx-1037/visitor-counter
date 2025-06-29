package com.nyx.visitorcounter.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nyx.visitorcounter.model.Console;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConsoleMapper extends BaseMapper<Console> {
}