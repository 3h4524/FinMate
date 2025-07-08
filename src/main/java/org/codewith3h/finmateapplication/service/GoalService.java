package org.codewith3h.finmateapplication.service;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.CreateGoalContributionRequest;
import org.codewith3h.finmateapplication.dto.request.CreateGoalRequest;
import org.codewith3h.finmateapplication.dto.request.GoalUpdateRequest;
import org.codewith3h.finmateapplication.dto.response.GoalResponse;
import org.codewith3h.finmateapplication.entity.Goal;
import org.codewith3h.finmateapplication.entity.GoalProgress;
import org.codewith3h.finmateapplication.enums.FeatureCode;
import org.codewith3h.finmateapplication.enums.LimitCount;
import org.codewith3h.finmateapplication.enums.Status;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.GoalContributionMapper;
import org.codewith3h.finmateapplication.mapper.GoalMapper;
import org.codewith3h.finmateapplication.mapper.GoalProgressMapper;
import org.codewith3h.finmateapplication.repository.GoalContributionRepository;
import org.codewith3h.finmateapplication.repository.GoalProgressRepository;
import org.codewith3h.finmateapplication.repository.GoalRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;


@Service
@Data
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GoalService {
    GoalRepository goalRepository;
    GoalProgressRepository goalProgressRepository;
    GoalMapper goalMapper;
    GoalProgressMapper goalProgressMapper;
    GoalContributionRepository goalContributionRepository;
    GoalContributionMapper goalContributionMapper;
    FeatureService featureService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @Transactional
    public GoalResponse createFinancialGoal(CreateGoalRequest request) {

        boolean hasUnlimited = featureService.userHasFeature(request.getUserId(), FeatureCode.UNLIMITED_GOAL.name());

        if (totalGoalInProgressByUserId(request.getUserId()) >= LimitCount.FINANCIAL_GOAL.getCount() && !hasUnlimited) {
            throw new AppException(ErrorCode.EXCEED_FREE_CREATE_GOAL);
        }

        log.info("Creating Financial Goal for userId: {} , goal name: {}, target: {}, deadline: {}",
                request.getUserId(), request.getName(), request.getTargetAmount(), request.getDeadline());

        Goal goalCreated = goalRepository.save(goalMapper.toGoal(request));
        GoalProgress goalProgress = goalProgressRepository.save(goalProgressMapper.toGoalProgress(goalCreated));
        log.info("goalCreated: {}", goalCreated);
        log.info("Creating Goal Progress for goalId: {}, progress date: {}",
                goalCreated.getId(), goalProgress.getProgressDate());

        if (goalCreated.getCurrentAmount().compareTo(BigDecimal.ZERO) > 0) {
            log.info("Creating new goal contribution for goalId {}, with note: Initial contribution, amount: {}",
                    goalCreated.getId(), goalCreated.getCurrentAmount());
            CreateGoalContributionRequest goalContributionRequest = new CreateGoalContributionRequest(goalCreated.getId(), goalCreated.getCurrentAmount(), "Initial contribution", LocalDate.now());
            goalContributionRepository.save(goalContributionMapper.toGoalContribution(goalContributionRequest));
        }

        updateStatusAfterContributeOrChange(goalCreated);
        log.info("Create goal finished: {}", goalCreated);
        return goalMapper.toGoalResponse(goalCreated);
    }

    private int totalGoalInProgressByUserId(Integer userId) {


        return goalRepository.countGoalsByUser_IdAndStatus(userId, Status.IN_PROGRESS.name());

    }

    @PreAuthorize("hasRole('ROLE_USER')")
    public void cancelFinancialGoal(Integer goalId) {

        Goal goal = goalRepository.findById(goalId).orElseThrow(() -> new AppException(ErrorCode.NO_GOAL_FOUND));

        log.info("Cancelling Financial Goal for goalId: {}, name: {}", goalId, goal.getName());

        goal.setStatus(Status.CANCELLED.name());

        goalRepository.save(goal);

        log.info("Financial Goal Canceled for goalId: {}, name: {}", goalId, goal.getName());

    }

    @PreAuthorize("hasRole('ROLE_USER')")
    public GoalResponse updateGoal(@Valid GoalUpdateRequest request, Integer goalId) {
        log.info("Updating Financial Goal for goalId: {}, name: {}", goalId, request.getName());

        Goal goal = goalRepository.findById(goalId).orElseThrow(() -> new AppException(ErrorCode.NO_GOAL_FOUND));

        goalMapper.updateGoal(goal, request);
        updateStatusAfterContributeOrChange(goal); // Kiểm tra trạng thái sau khi cập nhật
        log.info("Financial Goal Updated for goalId: {}, name: {}", goalId, request.getName());

        return goalMapper.toGoalResponse(goalRepository.save(goal));
    }

    public void updateStatusAfterContributeOrChange(Goal goal) {
        if (goal.getStatus() == null) {
            log.info("Goal status is null, set default status for goalId: {}, name: {}", goal.getId(), goal.getName());
            goal.setStatus(Status.IN_PROGRESS.name());
        }

        LocalDate today = LocalDate.now();
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0 &&
                !goal.getDeadline().isBefore(today) &&
                !Set.of(Status.COMPLETED.name(), Status.CANCELLED.name(), Status.FAILED.name()).contains(goal.getStatus())) {
            goal.setStatus(Status.COMPLETED.name());
            goalRepository.save(goal);
            log.info("Goal {} set to COMPLETED", goal.getId());
        } else if (goal.getDeadline().isBefore(today) &&
                goal.getCurrentAmount().compareTo(goal.getTargetAmount()) < 0 &&
                !Set.of(Status.COMPLETED.name(), Status.CANCELLED.name(), Status.FAILED.name()).contains(goal.getStatus())) {
            goal.setStatus(Status.FAILED.name());
            goalRepository.save(goal);
            log.info("Goal {} set to FAILED", goal.getId());
        } else if (!goal.getDeadline().isBefore(today) &&
                goal.getCurrentAmount().compareTo(goal.getTargetAmount()) < 0 &&
                goal.getStatus().equals(Status.FAILED.name())) {
            goal.setStatus(Status.IN_PROGRESS.name());
            goalRepository.save(goal);
            log.info("Goal {} set to IN_PROGRESS", goal.getId());
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    public GoalResponse getGoal(Integer goalId) {
        Goal goal = goalRepository.findById(goalId).orElseThrow(() -> new AppException(ErrorCode.NO_GOAL_FOUND));
        return goalMapper.toGoalResponse(goal);
    }

    public List<Goal> getAllGoals(){
        log.info("Fetching all goals");
        return goalRepository.findAll();
    }
}
