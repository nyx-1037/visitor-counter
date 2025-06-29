package com.nyx.visitorcounter.controller;

import com.nyx.visitorcounter.service.VisitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/visitor")
public class VisitorController {

    @Autowired
    private VisitorService visitorService;

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