package org.codewith3h.finmateapplication.scheduler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.entity.Goal;
import org.codewith3h.finmateapplication.enums.Status;
import org.codewith3h.finmateapplication.repository.GoalRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GoalProgressNotificationScheduler {

    GoalRepository goalRepository;
    // NotificationService notificationService;

    // Runs every 4 days at 00:00
    @Scheduled(cron = "0 0 0 */4 * ?")
    public void goalContributionReminderScheduler() {
        log.info("Starting goal contribution notification check");
        Pageable pageable = PageRequest.of(0, 100);
        int totalProcessed = 0;

        Page<Goal> goalPage;
        do {
            goalPage = goalRepository.findGoalByStatusAndNotificationEnabled(
                    Status.IN_PROGRESS.getStatusString(), true, pageable);
            totalProcessed += processGoals(goalPage);
            pageable = pageable.next();
        } while (goalPage.hasNext());

        log.info("Finished checking goal contributions, total {} goals processed.", totalProcessed);
    }

    private int processGoals(Page<Goal> goalPage) {
        int processed = 0;
        LocalDate today = LocalDate.now();

        for (Goal goal : goalPage.getContent()) {
            log.info("Checking goal {} for user {}", goal.getId(), goal.getUser().getId());
            if (!isValidGoal(goal, today)) {
                continue;
            }

            BigDecimal expectedAmountSoFar = calculateExpectedAmount(goal, today);
            if (expectedAmountSoFar != null && goal.getCurrentAmount().compareTo(expectedAmountSoFar) < 0) {
                String message = String.format("Your goal is behind schedule, expected: %s, actual: %s",
                        expectedAmountSoFar, goal.getCurrentAmount());
                // notificationService.sendNotification(goal.getUser().getId(), message);
            }
            processed++;
        }
        return processed;
    }

    private boolean isValidGoal(Goal goal, LocalDate today) {
        if (goal.getTargetAmount() == null || goal.getCurrentAmount() == null) {
            log.warn("Goal {} has null targetAmount or currentAmount", goal.getId());
            return false;
        }
        if (ChronoUnit.DAYS.between(goal.getStartDate(), goal.getDeadline()) <= 0) {
            log.warn("Goal {} has invalid date range (startDate: {}, deadline: {})",
                    goal.getId(), goal.getStartDate(), goal.getDeadline());
            return false;
        }
        if (ChronoUnit.DAYS.between(goal.getStartDate(), today) < 0) {
            log.warn("Goal {} has startDate after today: {}", goal.getId(), goal.getStartDate());
            return false;
        }
        return true;
    }

    private BigDecimal calculateExpectedAmount(Goal goal, LocalDate today) {
        long daysEstimate = ChronoUnit.DAYS.between(goal.getStartDate(), goal.getDeadline()) + 1;
        long daysPassed = ChronoUnit.DAYS.between(goal.getStartDate(), today) + 1;
        return goal.getTargetAmount()
                .divide(BigDecimal.valueOf(daysEstimate), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(daysPassed));
    }
}