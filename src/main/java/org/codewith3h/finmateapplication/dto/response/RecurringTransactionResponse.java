package org.codewith3h.finmateapplication.dto.response;

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
public class RecurringTransactionResponse {
    private Integer recurringId;
    private Integer categoryId;
    private Integer userCategoryId;
    private String categoryName;
    private String userCategoryName;
    private String type;
    private BigDecimal amount;
    private String note;
    private String  frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextDate;
    private Boolean isActive;
    private String icon;
}
