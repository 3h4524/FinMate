package org.codewith3h.finmateapplication.service;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.CreateGoalContributionRequest;
import org.codewith3h.finmateapplication.dto.response.GoalContributionResponse;
import org.codewith3h.finmateapplication.dto.response.GoalProgressResponse;
import org.codewith3h.finmateapplication.entity.GoalContribution;
import org.codewith3h.finmateapplication.mapper.GoalContributionMapper;
import org.codewith3h.finmateapplication.repository.GoalContributionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Data
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GoalContributionService {
    GoalContributionRepository goalContributionRepository;
    GoalContributionMapper goalContributionMapper;

    public Page<GoalContributionResponse> getContributionsByGoalId(int goalId, Pageable pageable) {
        Page<GoalContribution> goalContributionsPage = goalContributionRepository.findGoalContributionsByGoal_Id(goalId, pageable);

        List<GoalContributionResponse> responses = goalContributionsPage.getContent().stream().map(goalContributionMapper::toGoalContributionResponse).toList();

        return new PageImpl<>(responses, pageable, goalContributionsPage.getTotalElements());
    }

    public GoalContributionResponse createGoalContribution(CreateGoalContributionRequest request) {
        log.info("Creating goal contribution for goal id {}, amount: {}", request.getGoalId(), request.getAmount());
        GoalContribution goalContribution = goalContributionMapper.toGoalContribution(request);
        goalContributionRepository.save(goalContribution);
        return goalContributionMapper.toGoalContributionResponse(goalContribution);
    }
}
