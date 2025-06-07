package org.codewith3h.finmateapplication.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.codewith3h.finmateapplication.dto.response.MessageResponse;
import org.codewith3h.finmateapplication.dto.request.ForgotPasswordRequest;
import org.codewith3h.finmateapplication.dto.request.ResetPasswordRequest;
import org.codewith3h.finmateapplication.dto.request.LoginRequest;
import org.codewith3h.finmateapplication.dto.request.RegisterRequest;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import org.codewith3h.finmateapplication.exception.EmailNotVerifiedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@Validated
@Builder
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:3000", "http://127.0.0.1:8080", "http://127.0.0.1:3000"}, 
             allowedHeaders = "*", 
             allowCredentials = "true",
             methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String passwordResetConfirmationUrl;
    private final String googleClientId;

    public AuthController(
            UserService userService,
            PasswordEncoder passwordEncoder,
            @Value("${app.password.reset.confirmation-url}") String passwordResetConfirmationUrl,
            @Value("${google.oauth2.client-id}") String googleClientId) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetConfirmationUrl = passwordResetConfirmationUrl;
        this.googleClientId = googleClientId;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            log.info("Received registration request for email: {}", request.getEmail());
            Map<String, Object> response = userService.registerUser(request);
            
            if ((Boolean) response.get("success")) {
                if (response.containsKey("requiresVerification") && (Boolean) response.get("requiresVerification")) {
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.ok(new MessageResponse((String) response.get("message")));
                }
            } else {
                return ResponseEntity.badRequest().body(new MessageResponse((String) response.get("message")));
            }
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse("Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest, HttpSession session) {
        try {
            log.info("Received login request for email: {}", loginRequest.getEmail());
            Map<String, Object> response = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
            
            if ((Boolean) response.get("success")) {
                session.setAttribute("userEmail", response.get("email"));
                session.setAttribute("userId", response.get("userId"));
                session.setAttribute("userName", response.get("name"));
                session.setAttribute("userRole", response.get("role"));
                return ResponseEntity.ok(response);
            } else {
                if (response.containsKey("requiresVerification")) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }
                return ResponseEntity.badRequest().body(new MessageResponse((String) response.get("message")));
            }
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse("Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> payload, HttpSession session) {
        try {
            String idToken = payload.get("token");
            log.info("Received Google ID token");

            // Verify the token with Google
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response != null && response.has("email")) {
                String email = response.get("email").asText();
                String name = response.get("name").asText();
                boolean emailVerified = response.get("email_verified").asBoolean();

                Map<String, Object> responseData = userService.processGoogleLogin(email, name, emailVerified);
                
                if ((Boolean) responseData.get("success")) {
                    session.setAttribute("userId", responseData.get("userId"));
                    session.setAttribute("userEmail", responseData.get("email"));
                    session.setAttribute("userName", responseData.get("name"));
                    session.setAttribute("userRole", responseData.get("role"));
                    log.info("Session created for user ID: {}", responseData.get("userId"));
                }
                
                return ResponseEntity.ok(responseData);
            }

            return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid Google token"
            ));
        } catch (Exception e) {
            log.error("Google login failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Google login failed: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpSession session) {
        try {
            session.invalidate();
            log.info("User logged out successfully");
            return ResponseEntity.ok(new MessageResponse("Logout successful!"));
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse("Logout failed: " + e.getMessage()));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            log.info("Received resend verification request for email: {}", email);
            
            Map<String, Object> response = userService.resendVerificationEmail(email);
            log.debug("resendVerificationEmail: Service response success status: {}", response.get("success"));
            if ((Boolean) response.get("success")) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Resend verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "An error occurred while trying to resend the verification email."));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody Map<String, String> request, HttpSession session) {
        try {
            String email = request.get("email");
            String otp = request.get("otp");
            String code = request.get("code");
            String verificationCode = otp != null ? otp : code;
            
            log.info("Received email verification request for email: {}", email);
            
            Map<String, Object> response = userService.verifyEmail(email, verificationCode, session);
            if ((Boolean) response.get("success")) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(new MessageResponse((String) response.get("message")));
            }
        } catch (Exception e) {
            log.error("Email verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("An error occurred while verifying your email."));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        log.info("Received forgot password request for email: {}", email);
        
        Map<String, Object> response = userService.forgotPassword(email);
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        log.info("Received password reset request for token: {}", token);
        
        Map<String, Object> response = userService.processPasswordReset(token, newPassword);
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/check-auth")
    public ResponseEntity<?> checkAuth(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId != null) {
            Map<String, Object> userServiceResponse = userService.checkAuth(userId);
            if ((Boolean) userServiceResponse.get("isAuthenticated")) {
                return ResponseEntity.ok(userServiceResponse);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("isAuthenticated", false, "message", "User is not authenticated."));
    }

    @PostMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("exists", false));
            }

            boolean exists = userService.existsByEmail(email);
            return ResponseEntity.ok(Map.of("exists", exists));
        } catch (Exception e) {
            log.error("Error checking email: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("exists", false));
        }
    }

    @PostMapping("/set-password")
    public ResponseEntity<?> setPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");
        if (email == null || password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email and password are required");
        }
        User user = userService.findByEmail(email);
        if (user == null) return ResponseEntity.badRequest().body("User not found");

        // Update the user's password using userService.updateUserProfile
        Map<String, String> updates = new HashMap<>();
        updates.put("password", password);
        Map<String, Object> response = userService.updateUserProfile(user.getId(), updates);

        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(new MessageResponse((String) response.get("message")));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse((String) response.get("message")));
        }
    }
} 