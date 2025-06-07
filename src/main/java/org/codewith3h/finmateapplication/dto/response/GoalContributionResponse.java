package org.codewith3h.finmateapplication.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class GoalContributionResponse {
    Integer goalId;
    BigDecimal amount;
    String note;
    LocalDate contributionDate;
    Instant createdAt;
}

