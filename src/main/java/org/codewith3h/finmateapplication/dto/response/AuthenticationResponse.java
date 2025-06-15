package org.codewith3h.finmateapplication.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticationResponse {
    private String token;
    private boolean isVerified;
    private String email;
    private String name;
    private String role;
}
