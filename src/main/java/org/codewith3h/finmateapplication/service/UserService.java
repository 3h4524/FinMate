package org.codewith3h.finmateapplication.service;

import org.codewith3h.finmateapplication.dto.request.CreateUserRequest;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.repository.UserRepository;
import org.codewith3h.finmateapplication.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public User createUser(CreateUserRequest request) {
        logger.info("Creating new user with email: {}", request.getEmail());
        
        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Email already exists: {}", request.getEmail());
            throw new RuntimeException("Email already exists");
        }

        try {
            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPasswordHash(PasswordUtil.encode(request.getPassword()));
            user.setPremium(false);
            user.setRole("USER");

            // Lưu user vào database
            User savedUser = userRepository.save(user);
            entityManager.flush();
            entityManager.refresh(savedUser);

            logger.info("User created successfully with ID: {}", savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating user: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        logger.debug("Finding user by ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        logger.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
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
            boolean matches = PasswordUtil.matches(password, user.getPasswordHash());
            logger.debug("Password validation result for user {}: {}", email, matches);
            return matches;
        } catch (Exception e) {
            logger.error("Error validating user: {}", email, e);
            return false;
        }
    }

    @Transactional
    public void updateLastLogin(Long userId) {
        userRepository.updateLastLoginAt(userId);
    }

    @Transactional
    public User updateUser(Long id, Map<String, String> updates) {
        logger.info("Updating user with ID: {}", id);
        User user = findById(id);

        if (updates.containsKey("name")) {
            user.setName(updates.get("name"));
        }
        if (updates.containsKey("email")) {
            user.setEmail(updates.get("email"));
        }
        if (updates.containsKey("password")) {
            user.setPasswordHash(PasswordUtil.encode(updates.get("password")));
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

        logger.info("User updated successfully with ID: {}", updatedUser.getId());
        return updatedUser;
    }
}