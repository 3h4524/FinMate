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
//    NotificationService notificationService;

    // Chạy mỗi 4 ngày lúc 00:00
    @Scheduled(cron = "0 0 0 */4 * ?")
    public void goalContributionReminderScheduler() {
        log.info("Starting goal contribution notification check");
        LocalDate today = LocalDate.now();

        Pageable pageable = PageRequest.of(0, 100);
        Page<Goal> goalPage;
        int totalProcessed = 0;

        do {
            goalPage = goalRepository.findGoalByStatusAndNotificationEnabled(Status.IN_PROGRESS.getStatusString(), true, pageable);

            for (Goal goal : goalPage.getContent()) {
                log.info("Checking goal {} for user {}", goal.getId(), goal.getUser().getId());

                if (goal.getTargetAmount() == null || goal.getCurrentAmount() == null) {
                    log.warn("Goal {} has null targetAmount or currentAmount", goal.getId());
                    continue;
                }

                long daysEstimate = ChronoUnit.DAYS.between(goal.getStartDate(), goal.getDeadline()) + 1;
                if (daysEstimate <= 0) {
                    log.warn("Goal {} has invalid date range (startDate: {}, deadline: {})",
                            goal.getId(), goal.getStartDate(), goal.getDeadline());
                    continue;
                }

                BigDecimal averageAmountPerDay = goal.getTargetAmount()
                        .divide(BigDecimal.valueOf(daysEstimate), 2, RoundingMode.HALF_UP);

                long daysPassed = ChronoUnit.DAYS.between(goal.getStartDate(), today) + 1;
                if (daysPassed < 0) {
                    log.warn("Goal {} has startDate after today: {}", goal.getId(), goal.getStartDate());
                    continue;
                }

                BigDecimal expectedAmountSoFar = averageAmountPerDay.multiply(BigDecimal.valueOf(daysPassed));

                if (goal.getCurrentAmount().compareTo(expectedAmountSoFar) < 0) {
                    String message = "Your goals are behind schedule, expect: " + expectedAmountSoFar + ", actual: " + goal.getCurrentAmount();
//                    notificationService.sendNotification(goal.getUser().getId(), message);
                }
            }

            totalProcessed += goalPage.getNumberOfElements();
            pageable = pageable.next();
        } while (goalPage.hasNext());

        log.info("Finished checking goal contributions, total {} goals processed.", totalProcessed);
    }
}