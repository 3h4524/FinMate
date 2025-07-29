package org.codewith3h.finmateapplication.service;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.CreateGoalContributionRequest;
import org.codewith3h.finmateapplication.dto.response.GoalContributionResponse;
import org.codewith3h.finmateapplication.entity.Goal;
import org.codewith3h.finmateapplication.entity.GoalContribution;
import org.codewith3h.finmateapplication.enums.Status;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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

    public BigDecimal contributeByAgreement(Integer userId,BigDecimal amount){
        List<Goal> goals = goalRepository.findByUserIdAndStatusOrderByDeadline(userId, Status.IN_PROGRESS.name());
        int size = goals.size();
        int index = 0;
        while (amount.compareTo(BigDecimal.ZERO)>0 && index < size) {
            Goal goal = goals.get(index);
            BigDecimal remainingAmount = goal.getTargetAmount().subtract(goal.getCurrentAmount());
            if(remainingAmount.compareTo(amount) >= 0) {
                CreateGoalContributionRequest request = CreateGoalContributionRequest.builder()
                        .goalId(goal.getId())
                        .amount(amount)
                        .contributionDate(LocalDate.now())
                        .note("User contribute goal by income")
                        .build();
                createGoalContribution(request);
                amount = BigDecimal.ZERO;
            } else {
                CreateGoalContributionRequest request = CreateGoalContributionRequest.builder()
                        .goalId(goal.getId())
                        .amount(remainingAmount)
                        .contributionDate(LocalDate.now())
                        .note("User contribute goal by income")
                        .build();
                createGoalContribution(request);
                amount = amount.subtract(remainingAmount);
                log.info("remain: {}", amount);
                index++;
            }
        }
        return amount;
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
