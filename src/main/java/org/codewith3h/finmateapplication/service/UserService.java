package org.codewith3h.finmateapplication.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.RegisterRequest;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.UserDto;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Builder
public class UserService {

    private static final int VERIFICATION_CODE_LENGTH = 6;
    private static final String GOOGLE_AUTH = "GOOGLE_AUTH";
    private static final String PWD_RESET = "PWD_RESET_";
    private static final int MAX_RESEND_ATTEMPTS = 3;
    private static final int LOCKOUT_MINUTES = 10;
    private static final int VERIFICATION_CODE_EXPIRY_MINUTES = 10;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.password.reset.expiry-minutes}")
    private int passwordResetExpiryMinutes;

    @Transactional
    public ApiResponse<UserDto> registerUser(RegisterRequest request) {
        try {
            validateRegisterRequest(request);
            User user = createUser(request);
            log.info("User registered codefully: {}", user.getId());
            return ApiResponse.<UserDto>builder()
                    .code(1000)
                    .message("Registration codeful. Please verify your email.")
                    .result(toUserDto(user))
                    .build();
        } catch (AppException e) {
            log.error("Registration failed: {}", e.getMessage());
            return ApiResponse.<UserDto>builder()
                    .code(1000)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Transactional
    public ApiResponse<UserDto> loginUser(String email, String password) {
        try {
            if (!validateUser(email, password)) {
                return ApiResponse.<UserDto>builder()
                        .code(1000)
                        .message("Invalid email or password")
                        .build();
            }
            User user = findByEmail(email);
            updateLastLogin(user.getId());
            return ApiResponse.<UserDto>builder()
                    .code(1000)
                    .message("Login codeful")
                    .result(toUserDto(user))
                    .build();
        } catch (AppException e) {
            log.error("Login failed: {}", e.getMessage());
            return ApiResponse.<UserDto>builder()
                    .code(1000)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Transactional
    public ApiResponse<UserDto> processGoogleLogin(String email, String name, boolean emailVerified) {
        try {
            User user = findByEmail(email);
            if (user == null) {
                RegisterRequest request = RegisterRequest.builder()
                        .email(email)
                        .name(name)
                        .password(GOOGLE_AUTH)
                        .build();
                user = createUser(request);
                user.setVerified(emailVerified);
            } else if (!GOOGLE_AUTH.equals(user.getPasswordHash()) && emailVerified) {
                user.setVerified(true);
            }
            userRepository.save(user);
            updateLastLogin(user.getId());
            return ApiResponse.<UserDto>builder()
                    .code(1000)
                    .message("Google login codeful")
                    .result(toUserDto(user))
                    .build();
        } catch (Exception e) {
            log.error("Google login failed: {}", e.getMessage());
            return ApiResponse.<UserDto>builder()
                    .code(1000)
                    .message("Google login failed: " + e.getMessage())
                    .build();
        }
    }

    @Transactional
    public ApiResponse<UserDto> verifyEmail(String email, String verificationCode) {
        try {
            User user = findByEmail(email);
            if (user == null) {
                return ApiResponse.<UserDto>builder()
                        .code(1000)
                        .message("User not found")
                        .build();
            }
            if (user.isVerified()) {
                return ApiResponse.<UserDto>builder()
                        .code(1000)
                        .message("Email already verified")
                        .build();
            }
            if (isVerificationCodeInvalid(user)) {
                return ApiResponse.<UserDto>builder()
                        .code(1000)
                        .message("Invalid or expired verification code")
                        .build();
            }
            if (!emailService.verifyEmail(email, verificationCode)) {
                return ApiResponse.<UserDto>builder()
                        .code(1000)
                        .message("Invalid verification code")
                        .build();
            }

            user.setVerified(true);
            user.setResendAttempts(0);
            user.setResendLockoutUntil(null);
            user.setVerificationCode(null);
            user.setVerificationCodeExpiry(null);
            userRepository.save(user);

            return ApiResponse.<UserDto>builder()
                    .code(1000)
                    .message("Email verified codefully")
                    .result(toUserDto(user))
                    .build();
        } catch (Exception e) {
            log.error("Email verification failed: {}", e.getMessage());
            return ApiResponse.<UserDto>builder()
                    .code(1000)
                    .message("Failed to verify email")
                    .build();
        }
    }

    @Transactional
    public ApiResponse<String> resendVerificationEmail(String email) {
        try {
            User user = findByEmail(email);
            if (user == null) {
                return ApiResponse.<String>builder()
                        .code(1000)
                        .message("User not found")
                        .build();
            }
            if (user.isVerified()) {
                return ApiResponse.<String>builder()
                        .code(1000)
                        .message("Email already verified")
                        .build();
            }
            if (isInLockoutPeriod(user)) {
                return ApiResponse.<String>builder()
                        .code(1000)
                        .message("Please wait before requesting another verification code")
                        .build();
            }

            updateVerificationCode(user);
            emailService.sendVerificationEmail(email, user.getVerificationCode());
            return ApiResponse.<String>builder()
                    .code(1000)
                    .message("Verification code sent")
                    .build();
        } catch (Exception e) {
            log.error("Resend verification failed: {}", e.getMessage());
            return ApiResponse.<String>builder()
                    .code(1000)
                    .message("Failed to resend verification code")
                    .build();
        }
    }

    @Transactional
    public ApiResponse<String> forgotPassword(String email) {
        try {
            User user = findByEmail(email);
            if (user == null) {
                return ApiResponse.<String>builder()
                        .code(1000)
                        .message("Email not registered")
                        .build();
            }
            String resetToken = createPasswordResetToken(user);
            emailService.sendPasswordResetEmail(email, resetToken);
            return ApiResponse.<String>builder()
                    .code(1000)
                    .message("Password reset instructions sent")
                    .build();
        } catch (Exception e) {
            log.error("Forgot password failed: {}", e.getMessage());
            return ApiResponse.<String>builder()
                    .code(1000)
                    .message("Failed to process forgot password request")
                    .build();
        }
    }

    @Transactional
    public ApiResponse<String> processPasswordReset(String token, String newPassword) {
        try {
            token = extractToken(token);
            if (!token.startsWith(PWD_RESET)) {
                return ApiResponse.<String>builder()
                        .code(1000)
                        .message("Invalid token type")
                        .build();
            }
            resetPassword(token, newPassword);
            return ApiResponse.<String>builder()
                    .code(1000)
                    .message("Password reset codeful")
                    .build();
        } catch (Exception e) {
            log.error("Password reset failed: {}", e.getMessage());
            return ApiResponse.<String>builder()
                    .code(1000)
                    .message("Password reset failed: " + e.getMessage())
                    .build();
        }
    }

    @Transactional
    public ApiResponse<UserDto> updateUserPassword(String email, String password) {
        try {
            User user = findByEmail(email);
            if (user == null) {
                return ApiResponse.<UserDto>builder()
                        .code(1000)
                        .message("User not found")
                        .build();
            }
            user.setPasswordHash(passwordEncoder.encode(password));
            userRepository.save(user);
            return ApiResponse.<UserDto>builder()
                    .code(1000)
                    .message("Password updated codefully")
                    .result(toUserDto(user))
                    .build();
        } catch (Exception e) {
            log.error("Password update failed: {}", e.getMessage());
            return ApiResponse.<UserDto>builder()
                    .code(1000)
                    .message("Failed to update password")
                    .build();
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse<UserDto> getUserProfileByEmail(String email) {
        try {
            User user = findByEmail(email);
            if (user == null) {
                return ApiResponse.<UserDto>builder()
                        .code(1000)
                        .message("User not found")
                        .build();
            }
            return ApiResponse.<UserDto>builder()
                    .code(1000)
                    .message("Profile retrieved codefully")
                    .result(toUserDto(user))
                    .build();
        } catch (Exception e) {
            log.error("Get profile failed: {}", e.getMessage());
            return ApiResponse.<UserDto>builder()
                    .code(1000)
                    .message("Failed to get user profile")
                    .build();
        }
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private User createUser(RegisterRequest request) {
        validateRegisterRequest(request);
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED_EXCEPTION);
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isPremium(false)
                .role("USER")
                .verified(false)
                .resendAttempts(0)
                .build();

        return userRepository.save(user);
    }

    private UserDto toUserDto(User user) {
        return UserDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .premium(user.isPremium())
                .build();
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null ||
                request.getEmail() == null ||
                request.getPassword() == null ||
                request.getName() == null) {
            throw new IllegalArgumentException("Registration request cannot be null or have null fields");
        }
    }

    private boolean isVerificationCodeInvalid(User user) {
        return user.getVerificationCode() == null ||
                user.getVerificationCodeExpiry() == null ||
                user.getVerificationCodeExpiry().isBefore(LocalDateTime.now());
    }

    private boolean isInLockoutPeriod(User user) {
        return user.getResendLockoutUntil() != null &&
                user.getResendLockoutUntil().isAfter(LocalDateTime.now());
    }

    private void updateVerificationCode(User user) {
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRY_MINUTES));
        user.setResendAttempts(user.getResendAttempts() + 1);
        if (user.getResendAttempts() >= MAX_RESEND_ATTEMPTS) {
            user.setResendLockoutUntil(LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES));
        }
        userRepository.save(user);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < VERIFICATION_CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    private String createPasswordResetToken(User user) {
        String token = PWD_RESET + UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusMinutes(passwordResetExpiryMinutes));
        userRepository.save(user);
        return token;
    }

    private String extractToken(String token) {
        return token.contains("token=") ?
                token.substring(token.indexOf("token=") + 6) :
                token;
    }

    private void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (user.getPasswordResetTokenExpiry() == null ||
                user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    private boolean validateUser(String email, String password) {
        User user = findByEmail(email);
        if (user == null || GOOGLE_AUTH.equals(user.getPasswordHash())) {
            return false;
        }
        if (!user.isVerified()) {
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED_EXCEPTION);
        }
        return passwordEncoder.matches(password, user.getPasswordHash());
    }

    private void updateLastLogin(Integer userId) {
        userRepository.updateLastLoginAt(userId);
    }
}