package org.codewith3h.finmateapplication.service;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.codewith3h.finmateapplication.dto.request.CreateGoalContributionRequest;
import org.codewith3h.finmateapplication.dto.response.GoalContributionResponse;
import org.codewith3h.finmateapplication.entity.GoalContribution;
import org.codewith3h.finmateapplication.mapper.GoalContributionMapper;
import org.codewith3h.finmateapplication.repository.GoalContributionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GoalContributionService {
    GoalContributionRepository goalContributionRepository;
    GoalContributionMapper goalContributionMapper;

    public List<GoalContributionResponse> getContributionsByGoalId(int goalId) {
        return goalContributionRepository.findGoalContributionsByGoal_Id(goalId).stream().map(goalContributionMapper::toGoalContributionResponse).collect(Collectors.toList());
    }

    public GoalContributionResponse createGoalContribution(CreateGoalContributionRequest request) {
        GoalContribution goalContribution = goalContributionMapper.toGoalContribution(request);
        goalContributionRepository.save(goalContribution);
        return goalContributionMapper.toGoalContributionResponse(goalContribution);
    }
}
