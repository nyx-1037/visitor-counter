package com.nyx.visitorcounter.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nyx.visitorcounter.model.Console;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ConsoleMapper extends BaseMapper<Console> {
    /**
     * 分页查询日志列表，支持模糊查询
     * @param visitorId 访问量ID
     * @param ipAddress IP地址（模糊查询）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志列表
     */
    List<Console> findConsolesByPage(@Param("visitorId") Integer visitorId,
                                    @Param("ipAddress") String ipAddress,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);
}