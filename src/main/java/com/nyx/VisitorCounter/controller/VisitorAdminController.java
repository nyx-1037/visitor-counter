package com.nyx.visitorcounter.controller;

import com.nyx.visitorcounter.model.Visitor;
import com.nyx.visitorcounter.service.VisitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/visitors")
public class VisitorAdminController {

    @Autowired
    private VisitorService visitorService;

    @GetMapping
    public ResponseEntity<Iterable<Visitor>> getAllVisitors() {
        return ResponseEntity.ok(visitorService.getAllVisitors());
    }

    @PostMapping
    public ResponseEntity<Visitor> createVisitor(@RequestBody Visitor visitor) {
        return ResponseEntity.ok(visitorService.createVisitor(visitor));
    }

    @PutMapping
    public ResponseEntity<Visitor> updateVisitor(@RequestBody Visitor visitor) {
        return ResponseEntity.ok(visitorService.updateVisitor(visitor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVisitor(@PathVariable Integer id) {
        visitorService.deleteVisitor(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/syncRedisToMysql")
    public ResponseEntity<Void> syncRedisToMysql() {
        visitorService.syncRedisToMysql();
        return ResponseEntity.ok().build();
    }

    @PutMapping("/visitors/{id}/status")
    public ResponseEntity<Void> toggleVisitorStatus(@PathVariable Integer id, @RequestBody Visitor visitor) {
        visitorService.updateVisitorStatus(id, visitor.getStatus());
        return ResponseEntity.ok().build();
    }
}