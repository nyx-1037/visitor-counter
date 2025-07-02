package com.nyx.visitorcounter.controller;

import com.nyx.visitorcounter.service.ConsoleService;
import com.nyx.visitorcounter.service.UserService;
import com.nyx.visitorcounter.service.VisitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 控制台仪表板控制器
 * 提供仪表板统计数据和图表数据的API接口
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    @Autowired
    private VisitorService visitorService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ConsoleService consoleService;

    /**
     * 获取总访问量统计
     */
    @GetMapping("/stats/total-visitors")
    public ResponseEntity<Map<String, Object>> getTotalVisitors() {
        Map<String, Object> result = new HashMap<>();
        try {
            long totalVisitors = visitorService.getTotalVisitorCount();
            result.put("totalVisitors", totalVisitors);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("totalVisitors", 0);
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 获取网站数量统计
     */
    @GetMapping("/stats/total-sites")
    public ResponseEntity<Map<String, Object>> getTotalSites() {
        Map<String, Object> result = new HashMap<>();
        try {
            long totalSites = visitorService.getTotalSiteCount();
            result.put("totalSites", totalSites);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("totalSites", 0);
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 获取今日访问量统计
     */
    @GetMapping("/stats/today-visitors")
    public ResponseEntity<Map<String, Object>> getTodayVisitors() {
        Map<String, Object> result = new HashMap<>();
        try {
            long todayVisitors = consoleService.getTodayVisitCount();
            result.put("todayVisitors", todayVisitors);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("todayVisitors", 0);
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 获取用户数量统计
     */
    @GetMapping("/stats/total-users")
    public ResponseEntity<Map<String, Object>> getTotalUsers() {
        Map<String, Object> result = new HashMap<>();
        try {
            long totalUsers = userService.getTotalUserCount();
            result.put("totalUsers", totalUsers);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("totalUsers", 0);
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 获取访问量趋势数据（最近7天）
     */
    @GetMapping("/visit-trend")
    public ResponseEntity<Map<String, Object>> getVisitTrend() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> trendData = consoleService.getVisitTrend();
            List<String> dates = new ArrayList<>();
            List<Long> counts = new ArrayList<>();
            
            // 确保有7天的数据，缺失的日期补0
            for (int i = 6; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                dates.add(dateStr);
                
                // 查找对应日期的数据
                long count = 0;
                for (Map<String, Object> item : trendData) {
                    if (item.get("date") != null && item.get("date").toString().equals(dateStr)) {
                        count = Long.parseLong(item.get("count").toString());
                        break;
                    }
                }
                counts.add(count);
            }
            
            result.put("dates", dates);
            result.put("counts", counts);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("dates", Arrays.asList());
            result.put("counts", Arrays.asList());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 获取网站访问量分布数据
     */
    @GetMapping("/site-distribution")
    public ResponseEntity<List<Map<String, Object>>> getSiteDistribution() {
        try {
            return ResponseEntity.ok(visitorService.getSiteDistribution());
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    /**
     * 获取最近访问记录（最近10条）
     */
    @GetMapping("/recent-visits")
    public ResponseEntity<List<Map<String, Object>>> getRecentVisits() {
        try {
            return ResponseEntity.ok(consoleService.getRecentVisits(10));
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
}