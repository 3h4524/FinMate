package org.codewith3h.finmateapplication.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@ToString
public class UpdateBudgetRequest {

    @NotNull
    private Integer userId;

    private Integer categoryId;

    private Integer userCategoryId;

    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    private String periodType;

    private LocalDate startDate;

    @Min(value = 1)
    @Max(value = 100)
    private Integer notificationThreshold;
}