package com.example.store.Controller.AdminController;

import com.example.store.Entity.Role;
import com.example.store.Entity.User;
import com.example.store.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    // Lấy tất cả người dùng
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Gán role cho người dùng
    @PutMapping("/{userId}/roles")
    public ResponseEntity<User> updateUserRoles(
            @PathVariable Long userId,
            @RequestBody Set<String> roleNames) {
        User updatedUser = userService.assignRolesToUser(userId, roleNames);
        return ResponseEntity.ok(updatedUser);
    }

    // Lấy role của người dùng
    @GetMapping("/{userId}/roles")
    public ResponseEntity<Set<Role>> getUserRoles(@PathVariable Long userId) {
        Set<Role> roles = userService.getUserRoles(userId);
        return ResponseEntity.ok(roles);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        boolean isDeleted = userService.deleteUser(userId);
        if (isDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}

