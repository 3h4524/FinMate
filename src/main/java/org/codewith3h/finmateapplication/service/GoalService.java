package org.codewith3h.finmateapplication.service;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.codewith3h.finmateapplication.dto.request.CreateGoalRequest;
import org.codewith3h.finmateapplication.dto.response.GoalResponse;
import org.codewith3h.finmateapplication.entity.Goal;
import org.codewith3h.finmateapplication.mapper.GoalMapper;
import org.codewith3h.finmateapplication.mapper.GoalProgressMapper;
import org.codewith3h.finmateapplication.repository.GoalProgressRepository;
import org.codewith3h.finmateapplication.repository.GoalRepository;
import org.springframework.stereotype.Service;


@Service
@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GoalService {
    GoalRepository goalRepository;
    GoalProgressRepository goalProgressRepository;
    GoalMapper goalMapper;
    GoalProgressMapper goalProgressMapper;

    public GoalResponse createFinancialGoal(CreateGoalRequest request) {
        Goal goal = goalMapper.toGoal(request);
        goalRepository.save(goal);
        goalProgressRepository.save(goalProgressMapper.toGoalProgress(goal));
        return goalMapper.toGoalResponse(goal);
    }

}
