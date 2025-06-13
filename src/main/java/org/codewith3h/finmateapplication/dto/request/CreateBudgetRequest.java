package org.codewith3h.finmateapplication.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateBudgetRequest {

    @NotNull(message = "USER_ID_IS_REQUIRED")
    private Integer userId;

    @NotNull(message = "CATEGORY_ID_IS_REQUIRED")
    private Integer categoryId;

    private Integer userCategoryId;

    @NotNull(message = "AMOUNT_IS_REQUIRED")
    @DecimalMin(value = "0.01", inclusive = false, message = "AMOUNT_MUST_BE_POSITIVE")
    private BigDecimal amount;

    @NotNull(message = "PERIOD_TYPE_IS_REQUIRED")
    private String periodType;

    @NotNull(message = "START_DATE_IS_REQUIRED")
    private LocalDate startDate;

    @NotNull(message = "END_DATE_IS_REQUIRED")
    private LocalDate endDate;

    @NotNull(message = "NOTIFICATION_THRESHOLD_IS_REQUIRED")
    private Integer notificationThreshold;
}