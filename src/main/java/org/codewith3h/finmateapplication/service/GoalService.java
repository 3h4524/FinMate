package org.codewith3h.finmateapplication.service;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.CreateGoalRequest;
import org.codewith3h.finmateapplication.dto.response.GoalResponse;
import org.codewith3h.finmateapplication.entity.Goal;
import org.codewith3h.finmateapplication.entity.GoalProgress;
import org.codewith3h.finmateapplication.mapper.GoalMapper;
import org.codewith3h.finmateapplication.mapper.GoalProgressMapper;
import org.codewith3h.finmateapplication.repository.GoalProgressRepository;
import org.codewith3h.finmateapplication.repository.GoalRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
@Data
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GoalService {
    GoalRepository goalRepository;
    GoalProgressRepository goalProgressRepository;
    GoalMapper goalMapper;
    GoalProgressMapper goalProgressMapper;

    public GoalResponse createFinancialGoal(CreateGoalRequest request) {
        log.info("Creating Financial Goal for userId: {} , goal name: {}, target: {}, deadline: {}",
                request.getUserId(), request.getName(), request.getTargetAmount(), request.getDeadline());

        Goal goal = goalMapper.toGoal(request);
        goalRepository.save(goal);
        GoalProgress goalProgress = goalProgressRepository.save(goalProgressMapper.toGoalProgress(goal));

        log.info("Creating Goal Progress for goalId: {}, progress date: {}",
                goal.getId(), goalProgress.getProgressDate());

        if (goal.getCurrentAmount().compareTo(BigDecimal.ZERO) > 0)
            log.info("Creating new goal contribution for goalId {}, with note: Initial contribution, amount: {}",
                    goal.getId(), goal.getCurrentAmount());

        return goalMapper.toGoalResponse(goal);
    }

}
