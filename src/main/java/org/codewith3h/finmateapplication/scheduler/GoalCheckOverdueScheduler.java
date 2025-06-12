package org.codewith3h.finmateapplication.scheduler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.entity.Goal;
import org.codewith3h.finmateapplication.repository.GoalRepository;
import org.codewith3h.finmateapplication.service.GoalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GoalCheckOverdueScheduler {

    GoalRepository goalRepository;
    GoalService goalService;

    // Mỗi ngày kiểm tra các mục tiêu quá hạn lúc 00:00
    @Scheduled(cron = "0 0 0 * * ?")
    public void checkOverdue() {
        log.info("Checking overdue goals");
        LocalDate today = LocalDate.now();

        Pageable pageable = PageRequest.of(0, 100);
        Page<Goal> goalPage;
        int totalProcessed = 0;
        do {
            goalPage = goalRepository.findGoalByStatusAndDeadlineBefore("IN_PROGRESS", today, pageable);

            for (Goal goal : goalPage.getContent()) {
                log.info("Checking overdue goal {}", goal.getId());
                goalService.updateStatusAfterContributeOrChange(goal);
            }
            totalProcessed += goalPage.getNumberOfElements();
            pageable = pageable.next();
        } while (goalPage.hasNext());

        log.info("Finished checking overdue goals, total {} goals are handle.", totalProcessed);
    }
}