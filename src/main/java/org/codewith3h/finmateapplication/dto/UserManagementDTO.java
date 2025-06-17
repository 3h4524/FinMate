package org.codewith3h.finmateapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementDTO {
    private Integer id;
    private String name;
    private String email;
    private Boolean isPremium;
    private String role;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private Boolean verified;
    private Boolean isNewUser;
    private Boolean isDelete;
} 