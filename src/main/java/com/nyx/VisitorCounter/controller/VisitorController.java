package com.nyx.visitorcounter.controller;

import com.nyx.visitorcounter.service.VisitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 访问量统计前端控制器
 * 提供访问量统计相关的API接口，供前端页面调用
 */
@RestController
@RequestMapping("/api/visitor")
public class VisitorController {

    @Autowired
    private VisitorService visitorService;

    /**
     * 增加指定目标的访问量
     * 该接口允许跨域访问，用于前端页面嵌入统计代码时调用
     * 
     * @param target 目标标识符，用于区分不同的统计对象
     * @param request HTTP请求对象，用于获取访问者IP地址等信息
     * @return 返回更新后的访问量计数，如果目标不存在则返回404
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/increment")
    public ResponseEntity<java.util.Map<String, Long>> incrementVisitor(@RequestParam String target, HttpServletRequest request) {
        System.out.println("Received request for target: " + target);
        Long count = visitorService.incrementVisitorCount(target, request);
        if (count != -1L) {
            java.util.Map<String, Long> response = new java.util.HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}