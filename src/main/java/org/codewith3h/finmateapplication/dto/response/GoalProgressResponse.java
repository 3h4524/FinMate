package org.codewith3h.finmateapplication.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class GoalProgressResponse {
    Integer goalId;
    String name;
    String status;
    BigDecimal targetAmount;
    LocalDate deadline;
    LocalDate progressDate;
    BigDecimal amount;
    BigDecimal percentage;
    String timeRemaining;
}


