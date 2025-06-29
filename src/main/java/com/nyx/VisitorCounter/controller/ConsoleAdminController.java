package com.nyx.visitorcounter.controller;

import com.nyx.visitorcounter.model.Console;
import com.nyx.visitorcounter.service.ConsoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/consoles")
public class ConsoleAdminController {

    @Autowired
    private ConsoleService consoleService;

    @GetMapping
    public ResponseEntity<Iterable<Console>> getAllConsoles() {
        return ResponseEntity.ok(consoleService.getAllConsoles());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConsole(@PathVariable Integer id) {
        consoleService.deleteConsole(id);
        return ResponseEntity.ok().build();
    }
}