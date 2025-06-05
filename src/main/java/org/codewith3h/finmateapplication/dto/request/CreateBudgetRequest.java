package org.codewith3h.finmateapplication.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateBudgetRequest {
    private Integer userId;
    private Integer categoryId;
    private Integer userCategoryId;
    private BigDecimal amount;
    private String periodType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer notificationThreshold;
}