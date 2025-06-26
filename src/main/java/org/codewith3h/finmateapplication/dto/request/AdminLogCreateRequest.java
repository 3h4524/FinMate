package org.codewith3h.finmateapplication.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminLogCreateRequest {
    @NotNull
    private Integer adminId;

    @NotBlank
    private String action;

    @NotBlank
    private String entityType;

    private Integer entityId;

    private String details;
}
