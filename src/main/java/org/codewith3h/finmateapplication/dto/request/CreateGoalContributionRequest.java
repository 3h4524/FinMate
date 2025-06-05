package org.codewith3h.finmateapplication.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateGoalContributionRequest {
    private Integer goalId;
    private BigDecimal amount;
    private String note;
    private LocalDate contributionDate;
}
