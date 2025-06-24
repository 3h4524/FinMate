package org.codewith3h.finmateapplication.service;

import lombok.RequiredArgsConstructor;
import org.codewith3h.finmateapplication.dto.request.UpdateUserRequest;
import org.codewith3h.finmateapplication.dto.response.UserManagementResponse;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.mapper.UserManagementMapper;
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
    private final UserManagementMapper userManagementMapper;

    public Page<UserManagementResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userManagementMapper::toResponse);
    }

    public UserManagementResponse getUserById(Integer id) {
        return userRepository.findById(id)
                .map(userManagementMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public UserManagementResponse updateUser(Integer id, UpdateUserRequest userRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userManagementMapper.updateEntityFromRequest(userRequest, user);

        return userManagementMapper.toResponse(userRepository.save(user));
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
                .map(userManagementMapper::toResponse);
    }
} 