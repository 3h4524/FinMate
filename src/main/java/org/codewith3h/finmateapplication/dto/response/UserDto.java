package org.codewith3h.finmateapplication.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private Integer userId;
    private String email;
    private String name;
    private String role;
    private boolean isPremium;
    private String token;
    private boolean verified;
    private Boolean is2FAEnabled;
    private java.time.LocalDateTime lastLoginAt;
}