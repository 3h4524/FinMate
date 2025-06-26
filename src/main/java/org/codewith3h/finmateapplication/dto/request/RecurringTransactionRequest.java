package org.codewith3h.finmateapplication.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecurringTransactionRequest {
    @NotNull(message = "User ID is required")
    private Integer userId;
    private Integer categoryId;
    private Integer userCategoryId;
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;
    @Size(max = 255, message = "Note cannot exceed 255 characters")
    private String note;
    @NotNull(message = "Frequency is required")
    private String frequency;
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
