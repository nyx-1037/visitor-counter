package com.nyx.visitorcounter.controller;

import com.nyx.visitorcounter.model.Console;
import com.nyx.visitorcounter.model.PageResult;
import com.nyx.visitorcounter.service.ConsoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 访问日志管理控制器
 * 提供访问日志记录管理相关的API接口，仅管理员可访问
 */
@RestController
@RequestMapping("/api/admin/consoles")
public class ConsoleAdminController {

    @Autowired
    private ConsoleService consoleService;

    /**
     * 获取所有访问日志记录
     * 
     * @return 所有访问日志记录的列表
     */
    @GetMapping
    public ResponseEntity<Iterable<Console>> getAllConsoles() {
        return ResponseEntity.ok(consoleService.getAllConsoles());
    }
    
    /**
     * 分页获取访问日志记录，支持按访问量ID、IP地址和时间范围筛选
     * 
     * @param pageNum 页码，默认为1
     * @param pageSize 每页记录数，默认为10
     * @param visitorId 访问量记录ID筛选条件，可选
     * @param ipAddress IP地址筛选条件，可选
     * @param startTime 开始时间筛选条件，可选
     * @param endTime 结束时间筛选条件，可选
     * @return 分页访问日志记录列表
     */
    @GetMapping("/page")
    public ResponseEntity<PageResult<Console>> getConsolesByPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer visitorId,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(consoleService.getConsolesByPage(pageNum, pageSize, visitorId, ipAddress, startTime, endTime));
    }

    /**
     * 删除指定ID的访问日志记录
     * 
     * @param id 访问日志记录ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConsole(@PathVariable Integer id) {
        consoleService.deleteConsole(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 手动触发Redis数据同步到MySQL
     * 将Redis缓存中的访问日志数据同步到MySQL数据库
     * 
     * @return 无内容响应
     */
    @PostMapping("/syncRedisToMysql")
    public ResponseEntity<Void> syncRedisToMysql() {
        consoleService.syncRedisToMysql();
        return ResponseEntity.ok().build();
    }
}