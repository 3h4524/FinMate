package org.codewith3h.finmateapplication.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.LoginRequest;
import org.codewith3h.finmateapplication.dto.request.RegisterRequest;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.UserDto;
import org.codewith3h.finmateapplication.service.UserService;
import org.codewith3h.finmateapplication.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;

    @Value("${google.oauth2.client-id}")
    private String googleClientId;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Received registration request for email: {}", request.getEmail());
        return ResponseEntity.ok(userService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDto>> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Received login request for email: {}", loginRequest.getEmail());
        ApiResponse<UserDto> response = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
        if (response.getCode() == 1000) {
            UserDto user = response.getResult();
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
            user.setToken(token); // Add token to UserDto
        }
        return createResponse(response);
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<UserDto>> googleLogin(@RequestBody Map<String, String> payload) {
        try {
            String idToken = payload.get("token");
            log.info("Received Google ID token");
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response != null && response.has("email")) {
                String email = response.get("email").asText();
                String name = response.get("name").asText();
                boolean emailVerified = response.get("email_verified").asBoolean();

                ApiResponse<UserDto> responseData = userService.processGoogleLogin(email, name, emailVerified);
                if (responseData.isSuccess()) {
                    UserDto user = responseData.getData();
                    String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
                    user.setToken(token);
                }
                return createResponse(responseData);
            }
            return ResponseEntity.badRequest().body(ApiResponse.<UserDto>builder()
                    .success(false)
                    .message("Invalid Google token")
                    .build());
        } catch (Exception e) {
            log.error("Google login failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.<UserDto>builder()
                    .success(false)
                    .message("Google login failed: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> logout() {
        log.info("User logged out successfully");
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Logout successful")
                .build());
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<String>> resendVerificationEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        log.info("Received resend verification request for email: {}", email);
        return ResponseEntity.ok(userService.resendVerificationEmail(email));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<UserDto>> verifyEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String verificationCode = request.get("otp") != null ? request.get("otp") : request.get("code");
        log.info("Received email verification request for email: {}", email);

        ApiResponse<UserDto> response = userService.verifyEmail(email, verificationCode);
        if (response.isSuccess()) {
            UserDto user = response.getData();
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
            user.setToken(token);
        }
        return createResponse(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        log.info("Received forgot password request for email: {}", email);
        return ResponseEntity.ok(userService.forgotPassword(email));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        log.info("Received password reset request");
        return ResponseEntity.ok(userService.processPasswordReset(token, newPassword));
    }

    @GetMapping("/check-auth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDto>> checkAuth(@RequestHeader("Authorization") String token) {
        String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
        return ResponseEntity.ok(userService.getUserProfileByEmail(email));
    }

    @PostMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.<Boolean>builder()
                    .success(false)
                    .message("Email is required")
                    .data(false)
                    .build());
        }
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .success(true)
                .message("Email check completed")
                .data(exists)
                .build());
    }

    @PostMapping("/set-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDto>> setPassword(@RequestBody Map<String, String> payload,
                                                            @RequestHeader("Authorization") String token) {
        String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
        String password = payload.get("password");
        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.<UserDto>builder()
                    .success(false)
                    .message("Password is required")
                    .build());
        }
        return ResponseEntity.ok(userService.updateUserPassword(email, password));
    }

    private <T> ResponseEntity<ApiResponse<T>> createResponse(ApiResponse<T> response) {
        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.badRequest().body(response);
    }
}