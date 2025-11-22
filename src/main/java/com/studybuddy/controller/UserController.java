package com.studybuddy.controller;

import com.studybuddy.dto.UserResponse;
import com.studybuddy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        UserResponse user = userService.getUserProfile(authentication.getName());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me/stats")
    public ResponseEntity<Map<String, Object>> getCurrentUserStats(Authentication authentication) {
        Map<String, Object> stats = userService.getUserStats(authentication.getName());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me/modules")
    public ResponseEntity<Map<String, String>> updateModules(
            @RequestBody List<String> modules,
            Authentication authentication) {
        userService.updateModules(authentication.getName(), modules);
        return ResponseEntity.ok(Map.of("message", "Modules updated"));
    }
}
