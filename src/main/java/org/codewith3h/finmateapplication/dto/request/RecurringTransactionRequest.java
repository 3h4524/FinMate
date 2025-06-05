package org.codewith3h.finmateapplication.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jdk.jfr.Frequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecurringTransactionRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    private Long categoryId;
    private Long userCategoryId;
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;
    @Size(max = 255, message = "Note cannot exceed 255 characters")
    private String note;
    @NotNull(message = "Frequency is required")
    private Frequency frequency;
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    private Boolean isActive = true;
    @AssertTrue(message = "Exactly one of categoryId or userCategoryId must be provided")
    public boolean isCategoryValid(){
        return (categoryId == null) != (userCategoryId == null);
    }
}
