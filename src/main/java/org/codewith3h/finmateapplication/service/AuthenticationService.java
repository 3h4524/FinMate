package org.codewith3h.finmateapplication.service;

import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.RegisterRequest;
import org.codewith3h.finmateapplication.dto.request.ResetPasswordRequest;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.AuthenticationResponse;
import org.codewith3h.finmateapplication.dto.response.UserDto;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.UserMapper;
import org.codewith3h.finmateapplication.repository.UserRepository;
import org.codewith3h.finmateapplication.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private static final int VERIFICATION_CODE_LENGTH = 6;
    private static final String GOOGLE_AUTH = "GOOGLE_AUTH";
    private static final String PWD_RESET = "PWD_RESET_";
    private static final int MAX_RESEND_ATTEMPTS = 3;
    private static final int LOCKOUT_MINUTES = 10;
    private static final int VERIFICATION_CODE_EXPIRY_MINUTES = 10;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    private final UserMapper userMapper;

    @Value("${app.password.reset.expiry-minutes}")
    private int passwordResetExpiryMinutes;

    @Transactional
    public AuthenticationResponse loginUser(String email, String password) throws JOSEException {
            log.info("Attempting login for email: {}", email);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND_EXCEPTION));


            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                log.error("Login failed: Invalid password for email: {}", email);
                throw new AppException(ErrorCode.INVALID_PASSWORD);
            }

            userRepository.updateLastLoginAt(user.getId());
            log.info("Login successful for email: {}", email);

            // Generate token
            String token = jwtUtil.generateToken(user);
            if (token == null || token.isEmpty()) {
                log.error("Failed to generate token for user: {}", email);
            }

            return AuthenticationResponse.builder()
                    .token(token)
                    .isVerified(user.getVerified())
                    .name(user.getName())
                    .role(user.getRole())
                    .email(user.getEmail())
        .build();
    }

    public AuthenticationResponse processGoogleLogin(String email, String name, boolean emailVerified) throws JOSEException {
        log.info("Processing Google login for email: {}", email);
//         Check if user exists
            Optional<User> existingUser = userRepository.findByEmail(email);
            User user;

            if (existingUser.isPresent()) {
                log.info("User found, updating information");
                user = existingUser.get();
                // Update user information if needed
                if (!user.getName().equals(name)) {
                    user.setName(name);
                    user = userRepository.save(user);
                }
            } else {
                log.info("Creating new user from Google login");
                // Create new user with all required fields
                user = User.builder()
                        .name(name)
                        .email(email)
                        .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .role("USER")
                        .verified(emailVerified)
                        .isPremium(false)
                        .resendAttempts(0)
                        .verificationCode(null)
                        .verificationCodeExpiry(null)
                        .passwordResetToken(null)
                        .passwordResetTokenExpiry(null)
                        .resendLockoutUntil(null)
                        .build();
                user = userRepository.save(user);
            }

            // Generate token
            String token = jwtUtil.generateToken(user);

            AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                    .token(token)
                    .isVerified(user.getVerified())
                    .name(user.getName())
                    .role(user.getRole())
                    .email(user.getEmail())
                    .build();

            log.info("Google login successful for user: {}", user.getEmail());
            return authenticationResponse;

    }


    public ApiResponse<String> registerUser(RegisterRequest request) {
            log.info("Starting user registration for email: {}", request.getEmail());


            // Check if email already exists
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                log.error("Registration failed: Email already exists: {}", request.getEmail());
                throw new AppException(ErrorCode.EMAIL_EXISTED_EXCEPTION);
            }
            User user = userService.createUser(request);
            userRepository.save(user);
            log.info("User created successfully with ID: {}", user.getId());

            return ApiResponse.<String>builder()
                    .code(1000)
                    .message("Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản.")
                    .build();
    }
    @Transactional
    public AuthenticationResponse verifyEmail(String email, String verificationCode) throws JOSEException {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND_EXCEPTION));


            if (!emailService.verifyEmail(email, verificationCode)) {
                user.setResendAttempts(user.getResendAttempts() + 1);
            }

            user.setVerified(true);
            user.setResendAttempts(0);
            user.setResendLockoutUntil(null);
            user.setVerificationCode(null);
            user.setVerificationCodeExpiry(null);
            userRepository.save(user);


            return AuthenticationResponse.builder()
                    .isVerified(true)
                    .build();
    }


    @Transactional
    public ApiResponse<String> forgotPassword(String email) {
        try {
            emailService.sendPasswordResetEmail(email);
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
    public ApiResponse<String> processPasswordReset(ResetPasswordRequest request) {

        String token = request.getToken();
        String newPassword = request.getNewPassword();
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
                    .message("Password reset successfully")
                    .build();
        } catch (Exception e) {
            log.error("Password reset failed: {}", e.getMessage());
            return ApiResponse.<String>builder()
                    .code(1000)
                    .message("Password reset failed: " + e.getMessage())
                    .build();
        }
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


}
