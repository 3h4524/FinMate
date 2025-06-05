package org.codewith3h.finmateapplication.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
public class GoalContributionResponse {
    private Integer goalId;
    private BigDecimal amount;
    private String note;
    private LocalDate contributionDate;
    private Instant createdAt;
}

