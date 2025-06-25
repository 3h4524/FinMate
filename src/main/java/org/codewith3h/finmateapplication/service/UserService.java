package org.codewith3h.finmateapplication.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.codewith3h.finmateapplication.dto.request.RegisterRequest;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;


    public User createUser(RegisterRequest request) {
        if (request.getName() == null || request.getEmail() == null || request.getPassword() == null) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
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

    public boolean isInLockoutPeriod(User user) {
        return user.getResendLockoutUntil() != null &&
                user.getResendLockoutUntil().isAfter(LocalDateTime.now());
    }

    public void changePassword(Integer userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(java.time.LocalDateTime.now());
        userRepository.save(user);
    }
}