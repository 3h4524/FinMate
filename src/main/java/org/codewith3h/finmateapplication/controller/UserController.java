package org.codewith3h.finmateapplication.controller;

import org.codewith3h.finmateapplication.dto.request.RegisterRequest;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import lombok.Builder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Slf4j
@Validated
@Builder
@CrossOrigin(origins = "*")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        try {
            Map<String, Object> response = userService.getUserProfile(id);
            if ((Boolean) response.get("success")) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        try {
            Map<String, Object> response = userService.getUserProfileByEmail(email);
            if ((Boolean) response.get("success")) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "User not authenticated"));
            }
            Map<String, Object> response = userService.getUserProfile(userId);
            if ((Boolean) response.get("success")) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error getting user profile from session: ", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Failed to get user profile"));
        }
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody RegisterRequest request) {
        try {
            Map<String, Object> response = userService.createUserProfile(request);
            if ((Boolean) response.get("success")) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error creating user: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserProfile(@PathVariable Integer id, @RequestBody Map<String, String> updates) {
        log.info("Received update request for user ID: {}", id);
        try {
            Map<String, Object> response = userService.updateUserProfile(id, updates);
            if ((Boolean) response.get("success")) {
                log.info("User profile updated successfully for ID: {}", id);
                return ResponseEntity.ok(response);
            } else {
                log.error("Error updating user profile for ID: {}", id);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error updating user profile for ID: {}", id, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
