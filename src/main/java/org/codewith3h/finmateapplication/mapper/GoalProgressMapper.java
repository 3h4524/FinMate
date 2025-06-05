package org.codewith3h.finmateapplication.mapper;

import org.codewith3h.finmateapplication.dto.response.GoalProgressResponse;
import org.codewith3h.finmateapplication.entity.Goal;
import org.codewith3h.finmateapplication.entity.GoalProgress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface GoalProgressMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "goal", source = "goal")
    @Mapping(target = "amount", source = "goal.currentAmount")
    @Mapping(target = "progressDate", expression = "java(java.time.LocalDate.now())")
    @Mapping(target = "percentage", expression = "java(calculatePercentage(goal))")
    GoalProgress toGoalProgress(Goal goal);

    default BigDecimal calculatePercentage(Goal goal) {
        if (goal.getTargetAmount() == null || goal.getTargetAmount().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal percentage = goal.getCurrentAmount()
                .divide(goal.getTargetAmount(), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        // Cap the percentage at 100 if it exceeds 100
        return percentage.compareTo(BigDecimal.valueOf(100)) > 0 ? BigDecimal.valueOf(100) : percentage;
    }


    @Mapping(target = "id", source = "id")
    @Mapping(target = "goalId", source = "goal.id")
    @Mapping(target = "name", source = "goal.name")
    @Mapping(target = "status", source = "goal.status")
    @Mapping(target = "targetAmount", source = "goal.targetAmount")
    @Mapping(target = "deadline", source = "goal.deadline")
    @Mapping(target = "progressDate", source = "progressDate")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "percentage", source = "percentage")
    @Mapping(target = "isLongTerm", source = "goal.isLongTerm")
    @Mapping(target = "timeRemaining", expression = "java(calculateTimeRemaining(goalProgress.getGoal().getDeadline()))")
    GoalProgressResponse toGoalProgressResponse(GoalProgress goalProgress);

    default String calculateTimeRemaining(LocalDate deadline) {
        if (deadline == null) {
            return "N/A";
        }

        LocalDate today = LocalDate.now();
        if (today.isAfter(deadline)) {
            return "Overdue";
        }

        long days = java.time.temporal.ChronoUnit.DAYS.between(today, deadline);
        return days + " days remaining";
    }
}
