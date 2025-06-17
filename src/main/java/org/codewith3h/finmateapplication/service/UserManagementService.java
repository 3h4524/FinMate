package org.codewith3h.finmateapplication.service;

import lombok.RequiredArgsConstructor;
import org.codewith3h.finmateapplication.dto.UserManagementDTO;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.repository.UserManagementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserManagementRepository userRepository;

    public Page<UserManagementDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    public UserManagementDTO getUserById(Integer id) {
        return userRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public UserManagementDTO updateUser(Integer id, UserManagementDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setIsPremium(userDTO.getIsPremium());
        user.setRole(userDTO.getRole());
        user.setVerified(userDTO.getVerified());
        user.setIsNewUser(userDTO.getIsNewUser());

        return convertToDTO(userRepository.save(user));
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

    public Page<UserManagementDTO> searchUsers(String keyword, Pageable pageable) {
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
                .map(this::convertToDTO);
    }

    private UserManagementDTO convertToDTO(User user) {
        return UserManagementDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .isPremium(user.getIsPremium())
                .role(user.getRole())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .verified(user.getVerified())
                .isNewUser(user.getIsNewUser())
                .isDelete(user.isDelete())
                .build();
    }
} 