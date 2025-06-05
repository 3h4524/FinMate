package org.codewith3h.finmateapplication.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateGoalContributionRequest {
    private Integer goalId;
    private BigDecimal amount;
    private String note;
    private LocalDate contributionDate;
}
