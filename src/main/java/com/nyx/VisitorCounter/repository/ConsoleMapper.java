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
    
    /**
     * 统计今日访问量
     * @return 今日访问量
     */
    @org.apache.ibatis.annotations.Select("SELECT COUNT(*) FROM console_tb WHERE DATE(create_time) = CURDATE()")
    long countTodayVisits();
    
    /**
     * 获取最近7天的访问量趋势
     * @return 访问量趋势数据
     */
    @org.apache.ibatis.annotations.Select("SELECT DATE(create_time) as date, COUNT(*) as count " +
            "FROM console_tb " +
            "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY date ASC")
    List<java.util.Map<String, Object>> getVisitTrendLast7Days();
    
    /**
     * 获取最近访问记录
     * @param limit 记录数量限制
     * @return 最近访问记录列表
     */
    @org.apache.ibatis.annotations.Select("SELECT c.id, c.visitor_id, c.ip_address, c.create_time, v.target, v.description " +
            "FROM console_tb c " +
            "LEFT JOIN visitor_tb v ON c.visitor_id = v.id " +
            "ORDER BY c.create_time DESC " +
            "LIMIT #{limit}")
    List<java.util.Map<String, Object>> getRecentVisits(@Param("limit") int limit);
}