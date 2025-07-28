package org.codewith3h.finmateapplication.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateBudgetRequest {

    @NotNull
    private Integer userId;

    private Integer categoryId;

    private Integer userCategoryId;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = false)
    private BigDecimal amount;

    @NotNull
    private String periodType;

    @NotNull
    private LocalDate startDate;

    @NotNull
    @Min(value = 1)
    @Max(value = 100)
    private Integer notificationThreshold;
}