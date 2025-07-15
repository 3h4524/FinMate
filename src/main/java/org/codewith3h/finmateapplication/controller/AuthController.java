package org.codewith3h.finmateapplication.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.nimbusds.jose.JOSEException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.*;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.AuthenticationResponse;
import org.codewith3h.finmateapplication.dto.response.UserDto;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.repository.UserRepository;
import org.codewith3h.finmateapplication.service.AuthenticationService;
import org.codewith3h.finmateapplication.service.EmailService;
import org.codewith3h.finmateapplication.service.UserService;
import org.codewith3h.finmateapplication.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowCredentials = "false")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;
    private final AuthenticationService authenticationService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final UserService userService;

    @Value("${google.oauth2.client-id}")
    private String googleClientId;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());
        ApiResponse<String> response = authenticationService.registerUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(@Valid @RequestBody LoginRequest loginRequest) throws JOSEException {
        log.info("Login attempt for email: {}", loginRequest.getEmail());
        AuthenticationResponse response = authenticationService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
        if (!response.getIsVerified()) {
            // Trả về code riêng cho trường hợp chưa xác thực
            return ResponseEntity.ok(ApiResponse.<AuthenticationResponse>builder()
                    .code(1003)
                    .message("Email not verified")
                    .result(response)
                    .build());
        }
        return ResponseEntity.ok(ApiResponse.<AuthenticationResponse>builder()
                .code(1000)
                .message("Login successful")
                .result(response)
                .build());
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> googleLogin(@RequestBody Map<String, String> request) throws JOSEException {
        String idToken = request.get("token");
        if (idToken == null || idToken.isEmpty()) {
            log.warn("Empty Google token received");
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<AuthenticationResponse>builder()
                            .code(9999)
                            .message("Token không được để trống")
                            .build());
        }

        log.info("Verifying Google token");
            // Verify token with Google
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response == null) {
                log.warn("Null response from Google token verification");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<AuthenticationResponse>builder()
                                .code(9999)
                                .message("Không thể xác thực token với Google")
                                .build());
            }

            if (!response.has("email")) {
                log.warn("Invalid Google token response: {}", response.toString());
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<AuthenticationResponse>builder()
                                .code(9999)
                                .message("Token Google không hợp lệ")
                                .build());
            }

            String email = response.get("email").asText();
            String name = response.get("name").asText();
            boolean emailVerified = response.get("email_verified").asBoolean();

            log.info("Google token verified for email: {}", email);
            AuthenticationResponse responseData = authenticationService.processGoogleLogin(email, name, emailVerified);

            ApiResponse<AuthenticationResponse> responseClient = new ApiResponse<>();
            responseClient.setMessage("Google login successfully.");
            responseClient.setResult(responseData);
            log.info("Google login successful for user: {}", email);
            return ResponseEntity.ok(responseClient);
    }

    @GetMapping("/home")
    public ResponseEntity<ApiResponse<String>> redirectToHome(HttpServletRequest request) throws Exception {
        log.info("Received home page request.");

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Unauthorized access attempt to /home: Missing or invalid Authorization header");
            ApiResponse<String> response = new ApiResponse<>();
            response.setCode(9999);
            response.setMessage("Unauthorized: Invalid or missing token");
            response.setResult(null);
            return ResponseEntity.status(401).body(response);
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            log.warn("Unauthorized access attempt to /home: Invalid token");
            ApiResponse<String> response = new ApiResponse<>();
            response.setCode(9999);
            response.setMessage("Unauthorized: Invalid token");
            response.setResult(null);
            return ResponseEntity.status(401).body(response);
        }

        Integer userId = jwtUtil.extractId(token);
        if (userId == null) {
            log.warn("Unauthorized access attempt to /home: No email found in token");
            ApiResponse<String> response = new ApiResponse<>();
            response.setCode(9999);
            response.setMessage("Unauthorized: Invalid token format");
            response.setResult(null);
            return ResponseEntity.status(401).body(response);
        }

        log.info("User {} authorized to access home page", userId);
        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(1000);
        response.setMessage("Authorized");
        response.setResult("home.html");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "token", required = false) String tokenParam) {
        try {
            String token = null;
            
            // Try to get token from Authorization header first
            if (authorization != null && authorization.startsWith("Bearer ")) {
                token = authorization.substring(7);
            }
            // Fallback to token parameter (for sendBeacon)
            else if (tokenParam != null) {
                token = tokenParam;
            }
            
            if (token == null) {
                log.warn("No token provided for logout");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<String>builder()
                                .code(9999)
                                .message("No token provided")
                                .result(null)
                                .build());
            }
            
            String role = jwtUtil.extractRole(token);
            Integer userId = jwtUtil.extractId(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            log.info("Processing logout for user: {} with role: {}", userId, role);

            // Luôn set verified = false nếu là admin
            if (role.equalsIgnoreCase("ADMIN")) {
                user.setVerified(false);
                userRepository.save(user);
                log.info("Set verified = false for admin user: {}", userId);
            } else if (user.getIs2FAEnabled() != null && user.getIs2FAEnabled()) {
                user.setVerified(false);
                userRepository.save(user);
                log.info("Set verified = false for 2FA user: {}", userId);
            }

            jwtUtil.invalidateToken(token);
            SecurityContextHolder.clearContext();
            
            log.info("Logout successful for user: {}", userId);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .code(1000)
                    .message("Logout successful")
                    .result(null)
                    .build());
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<String>builder()
                            .code(9999)
                            .message("Logout failed: " + e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<String>> resendVerificationEmail(@RequestBody EmailRequest request) throws MessagingException {
        String email = request.getEmail();
        log.info("Received resend verification request for email: {}", email);
        emailService.sendVerificationEmail(email);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(1000)
                .message("Send otp successfully")
                .build());
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> verifyEmail(@RequestBody EmailRequest request) throws JOSEException {
        String email = request.getEmail();
        String verificationCode = request.getOtp() != null ? request.getOtp() : null;
        log.info("Received email verification request for email: {}", email);

        AuthenticationResponse authenticationResponse = authenticationService.verifyEmail(email, verificationCode);
        ApiResponse<AuthenticationResponse> response = new ApiResponse<>();
        response.setMessage("Email verified successfully");
        response.setResult(authenticationResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody EmailRequest request) {
        String email = request.getEmail();
        log.info("Received forgot password request for email: {}", email);
        return ResponseEntity.ok(authenticationService.forgotPassword(email));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordRequest request) {
            return ResponseEntity.ok(authenticationService.processPasswordReset(request));
    }

    @GetMapping("/verify-token")
    public ResponseEntity<ApiResponse<Boolean>> verifyToken(HttpServletRequest request) throws Exception {
        log.info("Received token verification request.");
        ApiResponse<Boolean> response = new ApiResponse<>();

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setResult(false);
            return ResponseEntity.ok(response);
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            response.setResult(false);
            return ResponseEntity.ok(response);
        }

        Integer userId = jwtUtil.extractId(token);
        if (userId == null) {
            response.setResult(false);
            return ResponseEntity.ok(response);
        }

        response.setResult(true);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-verification")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> sendEmailVerificationForUpdate(@RequestBody EmailRequest request) throws MessagingException {
        String email = request.getEmail();
        log.info("Received send verification request for email update: {}", email);
        emailService.sendVerificationEmail(email);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(1000)
                .message("Send otp successfully")
                .build());
    }

    @PostMapping("/verify-email-otp")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> verifyEmailOtp(@RequestBody EmailRequest request) {
        String email = request.getEmail();
        String verificationCode = request.getOtp();
        log.info("Received email verification request for email update: {}", email);

        boolean verified = emailService.verifyEmail(email, verificationCode);
        ApiResponse<Boolean> response = new ApiResponse<>();
        response.setCode(1000);
        response.setMessage("Email verified successfully");
        response.setResult(verified);
        return ResponseEntity.ok(response);
    }
}