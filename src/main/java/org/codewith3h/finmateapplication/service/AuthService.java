package org.codewith3h.finmateapplication.service;

import jakarta.servlet.http.HttpSession;
import org.codewith3h.finmateapplication.entity.User;

import java.util.Map;

public interface AuthService {
    User login(String email, String password, HttpSession session);
    User register(String name, String email, String password, HttpSession session);
    void logout(HttpSession session);
    boolean existsByEmail(String email);
    User updateUser(Integer id, Map<String, String> updates);
} 