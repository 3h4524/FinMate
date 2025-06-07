package org.codewith3h.finmateapplication.service.impl;

import jakarta.servlet.http.HttpSession;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.repository.UserRepository;
import org.codewith3h.finmateapplication.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User login(String email, String password, HttpSession session) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Lưu thông tin user vào session một cách nhất quán
        session.setAttribute("userId", user.getId());
        session.setAttribute("userEmail", user.getEmail());
        session.setAttribute("userName", user.getName());
        session.setAttribute("userRole", user.getRole());

        return user;
    }

    @Override
    public User register(String name, String email, String password, HttpSession session) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setLastLoginAt(LocalDateTime.now());

        user = userRepository.save(user);

        // Lưu thông tin user vào session sau khi đăng ký thành công
        session.setAttribute("userId", user.getId());
        session.setAttribute("userEmail", user.getEmail());
        session.setAttribute("userName", user.getName());
        session.setAttribute("userRole", user.getRole());

        return user;
    }

    @Override
    public void logout(HttpSession session) {
        session.invalidate();
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public User updateUser(Integer id, Map<String, String> updates) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        if (updates.containsKey("name")) {
            user.setName(updates.get("name"));
        }
        if (updates.containsKey("email")) {
            String newEmail = updates.get("email");
            if (newEmail != null && !newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(newEmail);
        }
        // Add other fields here as needed (e.g., password, role, etc.)
        // For password, you'd want a separate endpoint or handle carefully here.

        return userRepository.save(user);
    }
} 