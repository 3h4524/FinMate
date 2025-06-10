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
    private boolean premium;
}