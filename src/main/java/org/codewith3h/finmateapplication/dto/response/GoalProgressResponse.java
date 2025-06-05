package org.codewith3h.finmateapplication.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class GoalProgressResponse {
    private Integer id;
    private Integer goalId;
    private String name;
    private String status;
    private BigDecimal targetAmount;
    private LocalDate deadline;
    private LocalDate progressDate;
    private BigDecimal amount;
    private BigDecimal percentage;
    private String timeRemaining;
    private Boolean isLongTerm;

}


