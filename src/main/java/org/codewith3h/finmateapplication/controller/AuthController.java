package org.codewith3h.finmateapplication.controller;

import com.nimbusds.jose.JOSEException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.*;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.AuthenticationResponse;
import org.codewith3h.finmateapplication.dto.response.UserDto;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.service.AuthenticationService;
import org.codewith3h.finmateapplication.service.EmailService;
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
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class
AuthController {


    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;
    private final AuthenticationService authenticationService;
    private final EmailService emailService;

    @Value("${google.oauth2.client-id}")
    private String googleClientId;

    public AuthController(JwtUtil jwtUtil, RestTemplate restTemplate, AuthenticationService authenticationService, EmailService emailService) {
        this.jwtUtil = jwtUtil;
        this.restTemplate = restTemplate;
        this.authenticationService = authenticationService;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Received registration request for email: {}", request.getEmail());
        return ResponseEntity.ok(authenticationService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(@Valid @RequestBody LoginRequest loginRequest) throws JOSEException {
            log.info("Received login request for email: {}", loginRequest.getEmail());
            AuthenticationResponse authenticationResponse = authenticationService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
            
            ApiResponse<AuthenticationResponse> response = new ApiResponse<>();
            response.setCode(1000);
            response.setMessage("Login successfully");
            response.setResult(authenticationResponse);
            return ResponseEntity.ok(response);
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

        String userEmail = jwtUtil.extractEmail(token);
        if (userEmail == null) {
            log.warn("Unauthorized access attempt to /home: No email found in token");
            ApiResponse<String> response = new ApiResponse<>();
            response.setCode(9999);
            response.setMessage("Unauthorized: Invalid token format");
            response.setResult(null);
            return ResponseEntity.status(401).body(response);
        }

        log.info("User {} authorized to access home page", userEmail);
        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(1000);
        response.setMessage("Authorized");
        response.setResult("home.html");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    @Transactional
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
        ApiResponse<String> response = authenticationService.forgotPassword(email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        log.info("Received password reset request");
        ApiResponse<String> response = authenticationService.processPasswordReset(request);
        return ResponseEntity.ok(response);
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

        String userEmail = jwtUtil.extractEmail(token);
        if (userEmail == null) {
            response.setResult(false);
            return ResponseEntity.ok(response);
        }

        response.setResult(true);
        return ResponseEntity.ok(response);
    }
}