package com.nyx.visitorcounter.controller;

import com.nyx.visitorcounter.model.Console;
import com.nyx.visitorcounter.model.PageResult;
import com.nyx.visitorcounter.service.ConsoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/consoles")
public class ConsoleAdminController {

    @Autowired
    private ConsoleService consoleService;

    @GetMapping
    public ResponseEntity<Iterable<Console>> getAllConsoles() {
        return ResponseEntity.ok(consoleService.getAllConsoles());
    }
    
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConsole(@PathVariable Integer id) {
        consoleService.deleteConsole(id);
        return ResponseEntity.ok().build();
    }
}