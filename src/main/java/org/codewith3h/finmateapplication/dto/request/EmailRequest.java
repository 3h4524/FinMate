package org.codewith3h.finmateapplication.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailRequest {
    @NotBlank(message = "email is required")
    private String email;
    @NotBlank(message = "otp is required")
    private String otp;
}
