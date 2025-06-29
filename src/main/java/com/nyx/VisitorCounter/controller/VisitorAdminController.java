package com.nyx.visitorcounter.controller;

import com.nyx.visitorcounter.model.PageResult;
import com.nyx.visitorcounter.model.Visitor;
import com.nyx.visitorcounter.service.VisitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 访问量管理控制器
 * 提供访问量记录管理相关的API接口，仅管理员可访问
 */
@RestController
@RequestMapping("/api/admin/visitors")
public class VisitorAdminController {

    @Autowired
    private VisitorService visitorService;

    /**
     * 获取所有访问量记录
     * 
     * @return 所有访问量记录的列表
     */
    @GetMapping
    public ResponseEntity<Iterable<Visitor>> getAllVisitors() {
        return ResponseEntity.ok(visitorService.getAllVisitors());
    }
    
    /**
     * 分页获取访问量记录，支持按目标、描述和状态筛选
     * 
     * @param pageNum 页码，默认为1
     * @param pageSize 每页记录数，默认为10
     * @param target 目标筛选条件，可选
     * @param description 描述筛选条件，可选
     * @param status 状态筛选条件，可选
     * @return 分页访问量记录列表
     */
    @GetMapping("/page")
    public ResponseEntity<PageResult<Visitor>> getVisitorsByPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String target,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(visitorService.getVisitorsByPage(pageNum, pageSize, target, description, status));
    }

    /**
     * 创建新的访问量记录
     * 
     * @param visitor 访问量记录信息
     * @return 创建成功的访问量记录
     */
    @PostMapping
    public ResponseEntity<Visitor> createVisitor(@RequestBody Visitor visitor) {
        return ResponseEntity.ok(visitorService.createVisitor(visitor));
    }

    /**
     * 更新访问量记录
     * 
     * @param visitor 更新后的访问量记录信息
     * @return 更新成功的访问量记录
     */
    @PutMapping
    public ResponseEntity<Visitor> updateVisitor(@RequestBody Visitor visitor) {
        return ResponseEntity.ok(visitorService.updateVisitor(visitor));
    }

    /**
     * 删除指定ID的访问量记录
     * 
     * @param id 访问量记录ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVisitor(@PathVariable Integer id) {
        visitorService.deleteVisitor(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 手动触发Redis数据同步到MySQL
     * 将Redis缓存中的访问量数据同步到MySQL数据库
     * 
     * @return 无内容响应
     */
    @PostMapping("/syncRedisToMysql")
    public ResponseEntity<Void> syncRedisToMysql() {
        visitorService.syncRedisToMysql();
        return ResponseEntity.ok().build();
    }

    /**
     * 切换访问量记录状态（启用/禁用）
     * 
     * @param id 访问量记录ID
     * @param visitor 包含状态信息的访问量记录对象
     * @return 无内容响应
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> toggleVisitorStatus(@PathVariable Integer id, @RequestBody Visitor visitor) {
        visitorService.updateVisitorStatus(id, visitor.getStatus());
        return ResponseEntity.ok().build();
    }
}