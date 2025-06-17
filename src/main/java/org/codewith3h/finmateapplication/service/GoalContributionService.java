package org.codewith3h.finmateapplication.service;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.CreateGoalContributionRequest;
import org.codewith3h.finmateapplication.dto.response.GoalContributionResponse;
import org.codewith3h.finmateapplication.entity.Goal;
import org.codewith3h.finmateapplication.entity.GoalContribution;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.GoalContributionMapper;
import org.codewith3h.finmateapplication.repository.GoalContributionRepository;
import org.codewith3h.finmateapplication.repository.GoalRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Data
@Slf4j
@PreAuthorize("hasRole('ROLE_USER')")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GoalContributionService {
    GoalContributionRepository goalContributionRepository;
    GoalContributionMapper goalContributionMapper;
    GoalRepository goalRepository;
    GoalService goalService;

    public Page<GoalContributionResponse> getContributionsByGoalId(int goalId, Pageable pageable) {
        Page<GoalContribution> goalContributionsPage = goalContributionRepository.findGoalContributionsByGoal_Id(goalId, pageable);

        List<GoalContributionResponse> responses = goalContributionsPage.getContent().stream().map(goalContributionMapper::toGoalContributionResponse).toList();

        return new PageImpl<>(responses, pageable, goalContributionsPage.getTotalElements());
    }

    public GoalContributionResponse createGoalContribution(CreateGoalContributionRequest request) {

        Goal goal = goalRepository.findById(request.getGoalId()).orElseThrow(() -> new AppException(ErrorCode.NO_GOAL_FOUND));

        log.info("Creating goal contribution for goal id {}, amount: {}", goal.getId(), request.getAmount());
        GoalContribution goalContribution = goalContributionMapper.toGoalContribution(request);
        goalContributionRepository.save(goalContribution);

        goal.setCurrentAmount(goal.getCurrentAmount().add(request.getAmount()));
        goalRepository.save(goal);

        goalService.updateStatusAfterContributeOrChange(goal);

        return goalContributionMapper.toGoalContributionResponse(goalContribution);
    }
}
