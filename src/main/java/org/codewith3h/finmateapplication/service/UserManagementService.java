package org.codewith3h.finmateapplication.service;

import lombok.RequiredArgsConstructor;
import org.codewith3h.finmateapplication.dto.request.UserManagementRequest;
import org.codewith3h.finmateapplication.dto.response.UserManagementResponse;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.repository.UserManagementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UserManagementService {

    private final UserManagementRepository userRepository;

    public Page<UserManagementResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToResponse);
    }

    public UserManagementResponse getUserById(Integer id) {
        return userRepository.findById(id)
                .map(this::convertToResponse)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public UserManagementResponse updateUser(Integer id, UserManagementRequest userRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setIsPremium(userRequest.getIsPremium());
        user.setRole(userRequest.getRole());
        user.setVerified(userRequest.getVerified());

        return convertToResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsDelete(true);
        userRepository.save(user);
    }

    @Transactional
    public void restoreUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsDelete(false);
        userRepository.save(user);
    }

    public Page<UserManagementResponse> searchUsers(String keyword, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), likePattern),
                    cb.like(cb.lower(root.get("email")), likePattern)
                ));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return userRepository.findAll(spec, pageable)
                .map(this::convertToResponse);
    }

    private UserManagementResponse convertToResponse(User user) {
        return UserManagementResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .isPremium(user.getIsPremium())
                .role(user.getRole())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .verified(user.getVerified())
                .isNewUser(user.getIsNewUser())
                .isDelete(user.getIsDelete())
                .build();
    }
} 