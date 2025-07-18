package org.codewith3h.finmateapplication.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@ToString
public class UpdateBudgetRequest {

    @NotNull(message = "USER_ID_IS_REQUIRED")
    private Integer userId;

    private Integer categoryId;

    private Integer userCategoryId;

    @DecimalMin(value = "0.01", inclusive = false, message = "AMOUNT_MUST_BE_POSITIVE")
    private BigDecimal amount;

    private String periodType;

    private LocalDate startDate;

    private Integer notificationThreshold;
}