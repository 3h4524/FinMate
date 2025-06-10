package org.codewith3h.finmateapplication.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class GoalUpdateRequest {
    @NotBlank(message = "Goal name cannot be empty")
    @Size(max = 100, message = "Goal name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Target amount is required")
    @DecimalMin(value = "0.01", message = "Target amount must be greater than 0")
    private BigDecimal targetAmount;

    @NotNull(message = "Deadline is required")
    @FutureOrPresent(message = "Deadline cannot be in the past")
    private LocalDate deadline;

    private Boolean notificationEnabled = true;
}
