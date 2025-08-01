
package org.codewith3h.finmateapplication.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BudgetResponse {
    private Integer id;
    private Integer userId;
    private Integer categoryId;
    private String categoryName;
    private Integer userCategoryId;
    private String userCategoryName;
    private BigDecimal amount;
    private String periodType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer notificationThreshold;
    private BigDecimal currentSpending;
    private BigDecimal percentageUsed;
}