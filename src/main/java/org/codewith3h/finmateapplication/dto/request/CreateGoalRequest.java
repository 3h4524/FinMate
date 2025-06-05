package org.codewith3h.finmateapplication.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateGoalRequest {
    private String name;
    private Integer userId;
    private String description;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate startDate;
    private LocalDate deadline;
    private Boolean isLongTerm;
}

