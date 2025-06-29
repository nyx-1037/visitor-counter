package com.nyx.visitorcounter.controller;

import com.nyx.visitorcounter.model.Console;
import com.nyx.visitorcounter.model.PageResult;
import com.nyx.visitorcounter.model.User;
import com.nyx.visitorcounter.model.Visitor;
import com.nyx.visitorcounter.service.ConsoleService;
import com.nyx.visitorcounter.service.UserService;
import com.nyx.visitorcounter.service.VisitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    // User management
    @GetMapping("/users")
    public ResponseEntity<Iterable<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    @GetMapping("/users/page")
    public ResponseEntity<PageResult<User>> getUsersByPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(userService.getUsersByPage(pageNum, pageSize, username, status));
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.registerUser(user));
    }

    @PutMapping("/users")
    public ResponseEntity<User> updateUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<Void> toggleUserStatus(@PathVariable Integer id, @RequestBody User user) {
        userService.updateUserStatus(id, user.getStatus());
        return ResponseEntity.ok().build();
    }

}