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

/**
 * 管理员控制器
 * 提供用户管理相关的API接口，仅管理员可访问
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    /**
     * 获取所有用户列表
     * 
     * @return 所有用户的列表
     */
    @GetMapping("/users")
    public ResponseEntity<Iterable<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    /**
     * 分页获取用户列表，支持按用户名和状态筛选
     * 
     * @param pageNum 页码，默认为1
     * @param pageSize 每页记录数，默认为10
     * @param username 用户名筛选条件，可选
     * @param status 状态筛选条件，可选
     * @return 分页用户列表
     */
    @GetMapping("/users/page")
    public ResponseEntity<PageResult<User>> getUsersByPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(userService.getUsersByPage(pageNum, pageSize, username, status));
    }

    /**
     * 创建新用户
     * 
     * @param user 用户信息
     * @return 创建成功的用户信息
     */
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.registerUser(user));
    }

    /**
     * 更新用户信息
     * 
     * @param user 更新后的用户信息
     * @return 更新成功的用户信息
     */
    @PutMapping("/users")
    public ResponseEntity<User> updateUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(user));
    }

    /**
     * 删除指定ID的用户
     * 
     * @param id 用户ID
     * @return 无内容响应
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 切换用户状态（启用/禁用）
     * 
     * @param id 用户ID
     * @param user 包含状态信息的用户对象
     * @return 无内容响应
     */
    @PutMapping("/users/{id}/status")
    public ResponseEntity<Void> toggleUserStatus(@PathVariable Integer id, @RequestBody User user) {
        userService.updateUserStatus(id, user.getStatus());
        return ResponseEntity.ok().build();
    }

}