package com.nyx.visitorcounter.controller;

import com.nyx.visitorcounter.service.VisitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

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
    @CrossOrigin(
        origins = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
        allowedHeaders = "*",
        exposedHeaders = {"Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"},
        allowCredentials = "false",
        maxAge = 3600
    )
    @GetMapping("/increment")
    public ResponseEntity<Map<String, Long>> increment(@RequestParam String target, HttpServletRequest request) {
        System.out.println("Received request to increment visitor count for target: " + target);
        Long count = visitorService.incrementVisitorCount(target, request);
        Map<String, Long> response = new HashMap<>();
        
        if (count != -1L) {
            response.put("count", count);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 处理预检请求（OPTIONS）
     * 确保浏览器的CORS预检请求能够正确处理
     * 
     * @return 返回200状态码，允许后续的实际请求
     */
    @CrossOrigin(
        origins = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
        allowedHeaders = "*",
        exposedHeaders = {"Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"},
        allowCredentials = "false",
        maxAge = 3600
    )
    @RequestMapping(value = "/increment", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> handlePreflightRequest() {
        return ResponseEntity.ok().build();
    }
}