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
public class GoalResponse {

    String name;
    String description;
    String userId;
    BigDecimal currentAmount;
    BigDecimal targetAmount;
    LocalDate startDate;
    LocalDate deadline;
    String status;
    Boolean notificationEnabled;
    Integer priority;
}
