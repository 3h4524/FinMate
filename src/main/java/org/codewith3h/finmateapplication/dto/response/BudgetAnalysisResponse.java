package org.codewith3h.finmateapplication.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetAnalysisResponse {
    private Integer budgetId;
    private String categoryName;
    private BigDecimal plannedAmount;
    private BigDecimal actualSpending;
    private BigDecimal variance;
    private String periodType;
    private String status;
}