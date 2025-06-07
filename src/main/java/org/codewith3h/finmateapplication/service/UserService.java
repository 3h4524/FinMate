package org.codewith3h.finmateapplication.service;

import org.codewith3h.finmateapplication.dto.request.RegisterRequest;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.codewith3h.finmateapplication.exception.EmailNotVerifiedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.codewith3h.finmateapplication.service.EmailService;
import java.util.HashMap;
import jakarta.servlet.http.HttpSession;
import java.util.Random;
import java.lang.StringBuilder;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Value("${app.password.reset.expiry-minutes}")
    private int passwordResetExpiryMinutes;

    @Value("${app.password.reset.confirmation-url}")
    private String passwordResetConfirmationUrl;

    @PersistenceContext
    private EntityManager entityManager;

    // Private helper methods
    private User createUserEntity(RegisterRequest request) {
        logger.info("Creating new user entity with email: {}", request.getEmail());
        
        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Email already exists: {}", request.getEmail());
            throw new RuntimeException("Email already exists");
        }

        try {
            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user.setPremium(false);
            user.setRole("USER");
            user.setVerified(false);
            user.setResendAttempts(0);
            user.setResendLockoutUntil(null);
            user.setVerificationCode(null);
            user.setVerificationCodeExpiry(null);

            return userRepository.save(user);
        } catch (Exception e) {
            logger.error("Error creating user entity: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating user: " + e.getMessage());
        }
    }

    private User updateUserEntity(Integer id, Map<String, String> updates) {
        logger.info("Updating user entity with ID: {}", id);
        User user = getUserById(id);

        if (updates.containsKey("name")) {
            user.setName(updates.get("name"));
        }
        if (updates.containsKey("email")) {
            user.setEmail(updates.get("email"));
        }
        if (updates.containsKey("password")) {
            user.setPasswordHash(passwordEncoder.encode(updates.get("password")));
        }
        if (updates.containsKey("premium")) {
            user.setPremium(Boolean.parseBoolean(updates.get("premium")));
        }
        if (updates.containsKey("role")) {
            user.setRole(updates.get("role"));
        }

        User updatedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.refresh(updatedUser);

        logger.info("User entity updated successfully with ID: {}", updatedUser.getId());
        return updatedUser;
    }

    // Public methods
    @Transactional(readOnly = true)
    public User getUserById(Integer id) {
        logger.debug("Finding user by ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        logger.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email).orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        logger.debug("Checking if email exists: {}", email);
        return userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean validateUser(String email, String password) {
        logger.debug("Validating user with email: {}", email);
        try {
            User user = findByEmail(email);
            if (user == null) {
                return false;
            }

            if ("GOOGLE_AUTH".equals(user.getPasswordHash())) {
                logger.warn("Login failed: This is a Google account. Please use Google login.");
                return false;
            }

            boolean matches = passwordEncoder.matches(password, user.getPasswordHash());
            logger.debug("Password validation result for user {}: {}", email, matches);

            if (!matches) {
                return false;
            }

            if (!user.isVerified()) {
                logger.warn("Login failed: Email not verified for user: {}", email);
                throw new EmailNotVerifiedException("Email address is not verified.");
            }

            return true;
        } catch (EmailNotVerifiedException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error validating user {}: {}", email, e.getMessage(), e);
            return false;
        }
    }

    @Transactional
    public void updateLastLogin(Integer userId) {
        userRepository.updateLastLoginAt(userId);
    }

    @Transactional
    public String createPasswordResetTokenForUser(User user) {
        String token = "PWD_RESET_" + UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(passwordResetExpiryMinutes);
        
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiry(expiryDate);
        userRepository.save(user);
        
        return token;
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        logger.info("resetPassword: Received token: {}", token);
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> {
                    logger.warn("resetPassword: User not found for password reset token: {}", token);
                    return new RuntimeException("Invalid or expired token");
                });

        logger.info("resetPassword: Found user {} with password reset token. Expiry time: {}", user.getEmail(), user.getPasswordResetTokenExpiry());
        logger.info("resetPassword: Current time: {}", LocalDateTime.now());

        if (user.getPasswordResetTokenExpiry() == null || user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            logger.warn("resetPassword: Password reset token for user {} has expired or is null. Expiry: {}, Current: {}", user.getEmail(), user.getPasswordResetTokenExpiry(), LocalDateTime.now());
            throw new RuntimeException("Token has expired");
        }

        // No longer need to check token prefix here as findByPasswordResetToken ensures it.

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null); // Clear the reset token after successful use
        user.setPasswordResetTokenExpiry(null); // Clear the expiry as well
        userRepository.save(user);
        logger.info("resetPassword: Password successfully reset for user: {}", user.getEmail());
    }

    @Transactional
    public Map<String, Object> registerUser(RegisterRequest request) {
        logger.info("Processing registration request for email: {}", request.getEmail());
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = createUserEntity(request);
            logger.info("User registered successfully with ID: {}", user.getId());
            
            // Set initial verification state
            user.setVerified(false);
            user.setResendAttempts(0);
            user.setResendLockoutUntil(null);
            user.setVerificationCode(null);
            user.setVerificationCodeExpiry(null);
            userRepository.save(user);
            
            response.put("success", true);
            response.put("message", "Registration successful. Please verify your email.");
            response.put("userId", user.getId());
            response.put("requiresVerification", true);
            return response;
        } catch (Exception e) {
            logger.error("Registration failed: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }
    }

    @Transactional
    public Map<String, Object> loginUser(String email, String password) {
        logger.info("Processing login request for email: {}", email);
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (validateUser(email, password)) {
                User user = findByEmail(email);
                updateLastLogin(user.getId());
                
                response.put("success", true);
                response.put("message", "Login successful");
                response.put("userId", user.getId());
                response.put("email", user.getEmail());
                response.put("name", user.getName());
                response.put("role", user.getRole());
                response.put("premium", user.isPremium());
                return response;
            } else {
                response.put("success", false);
                response.put("message", "Invalid email or password");
                return response;
            }
        } catch (EmailNotVerifiedException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("requiresVerification", true);
            return response;
        } catch (Exception e) {
            logger.error("Login failed: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Login failed: " + e.getMessage());
            return response;
        }
    }

    @Transactional
    public Map<String, Object> processGoogleLogin(String email, String name, boolean emailVerified) {
        logger.info("Processing Google login for email: {}", email);
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = findByEmail(email);
            if (user == null) {
                // Create new user for Google login
                RegisterRequest request = new RegisterRequest();
                request.setEmail(email);
                request.setName(name);
                request.setPassword("GOOGLE_AUTH"); // Special marker for Google accounts
                
                user = createUserEntity(request);
                user.setVerified(emailVerified);
                user = userRepository.save(user);
                
                logger.info("Created new user for Google login with ID: {}", user.getId());
            } else if (!"GOOGLE_AUTH".equals(user.getPasswordHash())) {
                // Existing non-Google account: treat as successful login if email verified
                logger.info("Existing non-Google account found for email: {}. Proceeding as login.", email);
                // Ensure the account is verified before proceeding, if needed.
                // For Google login, if email is verified by Google, we can trust it.
                if (!user.isVerified() && emailVerified) {
                    user.setVerified(true);
                    userRepository.save(user);
                }
                // For existing non-Google accounts, set success to true and return user data
                response.put("success", true);
                response.put("message", "Login successful with existing account.");
                response.put("userId", user.getId());
                response.put("email", user.getEmail());
                response.put("name", user.getName());
                response.put("role", user.getRole());
                response.put("premium", user.isPremium());
                response.put("fullName", user.getName());
                updateLastLogin(user.getId()); // Update last login for existing users
                return response;
            } else {
                // Existing Google account, simply proceed.
                logger.info("Existing Google account found for email: {}. Proceeding.", email);
            }

            updateLastLogin(user.getId());
            
            response.put("success", true);
            response.put("message", "Google login successful");
            response.put("userId", user.getId());
            response.put("email", user.getEmail());
            response.put("name", user.getName());
            response.put("role", user.getRole());
            response.put("premium", user.isPremium());
            response.put("fullName", user.getName());
            return response;
        } catch (Exception e) {
            logger.error("Google login failed: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Google login failed: " + e.getMessage());
            return response;
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUserProfile(Integer id) {
        logger.info("Getting user profile for ID: {}", id);
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = getUserById(id);
            response.put("success", true);
            response.put("user", user);
            response.put("fullName", user.getName());
            return response;
        } catch (Exception e) {
            logger.error("Error getting user profile: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to get user profile");
            return response;
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUserProfileByEmail(String email) {
        logger.info("Getting user profile for email: {}", email);
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = findByEmail(email);
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }
            
            response.put("success", true);
            response.put("user", user);
            response.put("fullName", user.getName());
            return response;
        } catch (Exception e) {
            logger.error("Error getting user profile: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to get user profile");
            return response;
        }
    }

    @Transactional
    public Map<String, Object> createUserProfile(RegisterRequest request) {
        logger.info("Creating user profile for email: {}", request.getEmail());
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = createUserEntity(request);
            response.put("success", true);
            response.put("message", "User profile created successfully");
            response.put("user", user);
            return response;
        } catch (Exception e) {
            logger.error("Error creating user profile: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to create user profile: " + e.getMessage());
            return response;
        }
    }

    @Transactional
    public Map<String, Object> updateUserProfile(Integer id, Map<String, String> updates) {
        logger.info("Updating user profile for ID: {}", id);
        Map<String, Object> response = new HashMap<>();
        
        try {
            User updatedUser = updateUserEntity(id, updates);
            response.put("success", true);
            response.put("message", "User profile updated successfully");
            response.put("user", updatedUser);
            return response;
        } catch (Exception e) {
            logger.error("Error updating user profile: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to update user profile: " + e.getMessage());
            return response;
        }
    }

    @Transactional
    public Map<String, Object> resendVerificationEmail(String email) {
        logger.info("Processing resend verification email request for: {}", email);
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = findByEmail(email);
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }

            if (user.isVerified()) {
                response.put("success", false);
                response.put("message", "Email is already verified");
                return response;
            }

            // Check if user is in lockout period
            if (user.getResendLockoutUntil() != null && 
                user.getResendLockoutUntil().isAfter(LocalDateTime.now())) {
                response.put("success", false);
                response.put("message", "Please wait before requesting another verification code");
                return response;
            }

            // Generate new verification code (6 digits)
            Random random = new Random();
            StringBuilder verificationCode = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                verificationCode.append(random.nextInt(10));
            }
            
            user.setVerificationCode(verificationCode.toString());
            user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(10));
            
            // Update resend attempts
            user.setResendAttempts(user.getResendAttempts() + 1);
            if (user.getResendAttempts() >= 3) {
                user.setResendLockoutUntil(LocalDateTime.now().plusMinutes(10));
            }
            
            userRepository.save(user);

            // Send verification email
            try {
                emailService.sendVerificationEmail(email, verificationCode.toString());
            } catch (Exception e) {
                logger.error("Failed to send verification email: {}", e.getMessage(), e);
                response.put("success", false);
                response.put("message", "Failed to send verification email");
                return response;
            }

            response.put("success", true);
            response.put("message", "Verification code has been sent to your email");
            return response;
        } catch (Exception e) {
            logger.error("Resend verification failed: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to resend verification code");
            return response;
        }
    }

    @Transactional
    public Map<String, Object> verifyEmail(String email, String verificationCode, HttpSession session) {
        logger.info("Processing email verification for: {}", email);
        Map<String, Object> response = new HashMap<>();
        
        try {
        User user = findByEmail(email);
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }

            if (user.isVerified()) {
                response.put("success", false);
                response.put("message", "Email is already verified");
                return response;
            }

            // Check if verification code exists and is not expired
            if (user.getVerificationCode() == null || user.getVerificationCodeExpiry() == null) {
                response.put("success", false);
                response.put("message", "No verification code found. Please request a new one");
                return response;
            }

            if (user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
                response.put("success", false);
                response.put("message", "Verification code has expired. Please request a new one");
                return response;
            }

            // Verify email using EmailService
            boolean isVerified = emailService.verifyEmail(email, verificationCode);
            if (!isVerified) {
                response.put("success", false);
                response.put("message", "Invalid verification code");
                return response;
            }

            // Mark user as verified
            user.setVerified(true);
            user.setResendAttempts(0);
            user.setResendLockoutUntil(null);
            user.setVerificationCode(null);
            user.setVerificationCodeExpiry(null);
            userRepository.save(user);

            // Set session attributes
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getName());
            session.setAttribute("userRole", user.getRole());

            response.put("success", true);
            response.put("message", "Email verified successfully");
            response.put("userId", user.getId());
            response.put("email", user.getEmail());
            response.put("name", user.getName());
            response.put("role", user.getRole());
            response.put("premium", user.isPremium());
            return response;
        } catch (Exception e) {
            logger.error("Error verifying email: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to verify email");
            return response;
        }
    }

    @Transactional
    public Map<String, Object> forgotPassword(String email) {
        logger.info("Processing forgot password request for email: {}", email);
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = findByEmail(email);
            if (user == null) {
                logger.warn("Forgot password request for non-existent email: {}", email);
                response.put("success", false);
                response.put("message", "This email is not registered. Please sign up first.");
                return response;
            }

            // Generate password reset token
            String resetToken = createPasswordResetTokenForUser(user);
            // Do not construct the full URL here; pass only the token to EmailService
            // EmailService will construct the full URL with its base URL configuration
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
            logger.info("Password reset token created and email sent for: {}", email);
            
            response.put("success", true);
            response.put("message", "Password reset instructions sent to your email.");
            return response;
        } catch (Exception e) {
            logger.error("Error processing forgot password request: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "An error occurred during the forgot password process.");
            return response;
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> checkAuth(Integer userId) {
        logger.info("Checking authentication status for user ID: {}", userId);
        Map<String, Object> response = new HashMap<>();
        
        if (userId == null) {
            response.put("isAuthenticated", false);
            response.put("message", "User ID is null.");
            return response;
        }

        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            response.put("isAuthenticated", true);
            response.put("message", "User is authenticated.");
            response.put("userId", user.getId());
            response.put("email", user.getEmail());
            response.put("name", user.getName()); // Assuming User entity has a 'name' field
            response.put("role", user.getRole());
            response.put("premium", user.isPremium());
        } else {
            response.put("isAuthenticated", false);
            response.put("message", "User not found.");
        }
        return response;
    }

    @Transactional
    public Map<String, Object> processPasswordReset(String token, String newPassword) {
        logger.info("Processing password reset request for token: {}", token);
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extract token from URL if needed
            if (token.contains("token=")) {
                token = token.substring(token.indexOf("token=") + 6);
            }
            
            // Verify that this is a password reset token
            if (!token.startsWith("PWD_RESET_")) {
                response.put("success", false);
                response.put("message", "Invalid token type");
                return response;
            }

            // Reset password
            resetPassword(token, newPassword);
            logger.info("Password reset successfully for token: {}", token);
            
            response.put("success", true);
            response.put("message", "Password has been reset successfully.");
            return response;
        } catch (Exception e) {
            logger.error("Error resetting password: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Password reset failed: " + e.getMessage());
            return response;
        }
    }
}