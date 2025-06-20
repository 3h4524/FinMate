package org.codewith3h.finmateapplication.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetAnalysisResponse {
    private Integer budgetId;
    private String categoryName;
    private String userCategoryName;
    private BigDecimal plannedAmount;
    private BigDecimal actualSpending;
    private String periodType;
    private String status;
}