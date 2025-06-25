package org.codewith3h.finmateapplication.controller;

import lombok.RequiredArgsConstructor;
import org.codewith3h.finmateapplication.dto.request.RegisterRequest;
import org.codewith3h.finmateapplication.dto.request.UpdateUserRequest;
import org.codewith3h.finmateapplication.dto.request.ChangePasswordRequest;
import org.codewith3h.finmateapplication.dto.request.EmailRequest;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.UserDto;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.repository.UserRepository;
import org.codewith3h.finmateapplication.service.UserService;
import org.codewith3h.finmateapplication.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import org.codewith3h.finmateapplication.service.EmailService;

@RestController
@RequestMapping("/api/v1/users")
@Slf4j
@Validated
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowCredentials = "false")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> getUserProfile(HttpServletRequest request) {
        log.info("Received get user profile request.");
        ApiResponse<UserDto> response = new ApiResponse<>();

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setCode(9999);
            response.setMessage("Unauthorized: Invalid or missing token");
            return ResponseEntity.status(401).body(response);
        }

        String token = authHeader.substring(7);
        try {
            if (!jwtUtil.validateToken(token)) {
                response.setCode(9999);
                response.setMessage("Unauthorized: Invalid token");
                return ResponseEntity.status(401).body(response);
            }

            Integer userId = jwtUtil.extractId(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            UserDto userDto = UserDto.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole())
                    .isPremium(user.getIsPremium())
                    .verified(user.getVerified())
                    .is2FAEnabled(user.getIs2FAEnabled())
                    .lastLoginAt(user.getLastLoginAt())
                    .build();

            response.setCode(1000);
            response.setMessage("Success");
            response.setResult(userDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting user profile: {}", e.getMessage());
            response.setCode(9999);
            response.setMessage("Error getting user profile");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/profile")
    @Transactional
    public ResponseEntity<ApiResponse<UserDto>> updateUserProfile(@RequestBody @Validated UpdateUserRequest updateRequest,
                                                                  HttpServletRequest request) {
        log.info("Received update user profile request: {}", updateRequest);
        ApiResponse<UserDto> response = new ApiResponse<>();

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setCode(9999);
            response.setMessage("Unauthorized: Invalid or missing token");
            return ResponseEntity.status(401).body(response);
        }

        String token = authHeader.substring(7);
        try {
            if (!jwtUtil.validateToken(token)) {
                response.setCode(9999);
                response.setMessage("Unauthorized: Invalid token");
                return ResponseEntity.status(401).body(response);
            }

            Integer userId = jwtUtil.extractId(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            // Validate email format
            if (updateRequest.getEmail() != null && !updateRequest.getEmail().isBlank()) {
                if (!updateRequest.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    throw new AppException(ErrorCode.INVALID_EMAIL_FORMAT);
                }
                // Check if email exists for another user
                if (!user.getEmail().equals(updateRequest.getEmail()) && 
                    userRepository.existsByEmail(updateRequest.getEmail())) {
                    throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
                }
            }

            // Only update provided fields to avoid overwriting existing data
            if (updateRequest.getName() != null && !updateRequest.getName().isBlank()) {
                user.setName(updateRequest.getName());
            }
            boolean emailChanged = false;
            if (updateRequest.getEmail() != null && !updateRequest.getEmail().isBlank()
                && !user.getEmail().equals(updateRequest.getEmail())) {
                // Gửi OTP về email mới, không update DB
                emailService.sendVerificationEmail(updateRequest.getEmail());
                emailChanged = true;
            }
            if (!emailChanged) {
                user.setUpdatedAt(java.time.LocalDateTime.now());
                userRepository.save(user);
            }
            // Log the update
            log.info("User profile updated successfully. UserId: {}, Name: {}, Email: {}", 
                    user.getId(), user.getName(), user.getEmail());
            UserDto userDto = UserDto.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole())
                    .isPremium(user.getIsPremium())
                    .verified(user.getVerified())
                    .is2FAEnabled(user.getIs2FAEnabled())
                    .lastLoginAt(user.getLastLoginAt())
                    .build();
            if (emailChanged) {
                response.setCode(2001);
                response.setMessage("Please verify your new email address. We have sent a verification code to your new email.");
            } else {
                response.setCode(1000);
                response.setMessage("Profile updated successfully");
            }
            response.setResult(userDto);
            return ResponseEntity.ok(response);
        } catch (AppException ex) {
            log.error("Error updating profile: {}", ex.getMessage());
            response.setCode(ex.getErrorCode().getCode());
            response.setMessage(ex.getMessage());
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            log.error("Unexpected error updating profile: {}", e.getMessage(), e);
            response.setCode(9999);
            response.setMessage("Error updating user profile");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@RequestBody @Validated ChangePasswordRequest requestBody,
                                                            HttpServletRequest request) {
        ApiResponse<Void> response = new ApiResponse<>();

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setCode(9999);
            response.setMessage("Unauthorized");
            return ResponseEntity.status(401).body(response);
        }

        String token = authHeader.substring(7);
        try {
            if (!jwtUtil.validateToken(token)) {
                response.setCode(9999);
                response.setMessage("Invalid token");
                return ResponseEntity.status(401).body(response);
            }

            Integer userId = jwtUtil.extractId(token);

            userService.changePassword(userId, requestBody.getCurrentPassword(), requestBody.getNewPassword());
            response.setCode(1000);
            response.setMessage("Password updated successfully");
            return ResponseEntity.ok(response);
        } catch (AppException ex) {
            response.setCode(ex.getErrorCode().getCode());
            response.setMessage(ex.getMessage());
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            response.setCode(9999);
            response.setMessage("Error changing password");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/profile/2fa")
    public ResponseEntity<ApiResponse<Boolean>> update2FA(@RequestHeader("Authorization") String authorization,
                                                         @RequestBody Map<String, Boolean> body) {
        try {
            String token = authorization.substring(7);
            Integer userId = jwtUtil.extractId(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            Boolean enable2FA = body.get("is2FAEnabled");
            user.setIs2FAEnabled(enable2FA);
            userRepository.save(user);
            return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                    .code(1000)
                    .message("Cập nhật 2FA thành công")
                    .result(enable2FA)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<Boolean>builder()
                    .code(9999)
                    .message("Lỗi xác thực: " + e.getMessage())
                    .result(false)
                    .build());
        }
    }

    @PostMapping("/profile/verify-email-change")
    public ResponseEntity<ApiResponse<UserDto>> verifyEmailChange(@RequestBody EmailRequest request, HttpServletRequest httpRequest) {
        log.info("Received verify email change request for email: {}", request.getEmail());
        ApiResponse<UserDto> response = new ApiResponse<>();
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setCode(9999);
            response.setMessage("Unauthorized: Invalid or missing token");
            return ResponseEntity.status(401).body(response);
        }
        String token = authHeader.substring(7);
        try {
            if (!jwtUtil.validateToken(token)) {
                response.setCode(9999);
                response.setMessage("Unauthorized: Invalid token");
                return ResponseEntity.status(401).body(response);
            }
            Integer userId = jwtUtil.extractId(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            // Xác thực OTP qua EmailVerification
            boolean verified = emailService.verifyEmail(request.getEmail(), request.getOtp());
            if (!verified) {
                response.setCode(2002);
                response.setMessage("Invalid or expired verification code");
                return ResponseEntity.badRequest().body(response);
            }
            // Update email nếu xác thực thành công
            user.setEmail(request.getEmail());
            user.setVerified(true);
            user.setUpdatedAt(java.time.LocalDateTime.now());
            userRepository.save(user);
            log.info("User {} updated email to {} successfully", userId, request.getEmail());
            UserDto userDto = UserDto.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole())
                    .isPremium(user.getIsPremium())
                    .verified(user.getVerified())
                    .is2FAEnabled(user.getIs2FAEnabled())
                    .lastLoginAt(user.getLastLoginAt())
                    .build();
            response.setCode(1000);
            response.setMessage("Email updated and verified successfully");
            response.setResult(userDto);
            return ResponseEntity.ok(response);
        } catch (AppException ex) {
            log.error("Error verifying email change: {}", ex.getMessage());
            response.setCode(ex.getErrorCode().getCode());
            response.setMessage(ex.getMessage());
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            log.error("Unexpected error verifying email change: {}", e.getMessage(), e);
            response.setCode(9999);
            response.setMessage("Error verifying email change");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/profile/send-otp-change-email")
    public ResponseEntity<ApiResponse<String>> sendOtpChangeEmail(@RequestBody EmailRequest request, HttpServletRequest httpRequest) {
        log.info("Received request to send OTP for change email: {}", request.getEmail());
        ApiResponse<String> response = new ApiResponse<>();
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setCode(9999);
            response.setMessage("Unauthorized: Invalid or missing token");
            return ResponseEntity.status(401).body(response);
        }
        String token = authHeader.substring(7);
        try {
            if (!jwtUtil.validateToken(token)) {
                response.setCode(9999);
                response.setMessage("Unauthorized: Invalid token");
                return ResponseEntity.status(401).body(response);
            }
            Integer userId = jwtUtil.extractId(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            // Check email exists
            if (userRepository.existsByEmail(request.getEmail())) {
                response.setCode(2003);
                response.setMessage("Email already exists");
                response.setResult(null);
                return ResponseEntity.badRequest().body(response);
            }
            emailService.sendOtpForChangeEmail(request.getEmail(), user);
            response.setCode(1000);
            response.setMessage("OTP sent to new email successfully");
            response.setResult("OK");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending OTP for change email: {}", e.getMessage());
            response.setCode(9999);
            response.setMessage("Failed to send OTP: " + e.getMessage());
            response.setResult(null);
            return ResponseEntity.status(500).body(response);
        }
    }
}
