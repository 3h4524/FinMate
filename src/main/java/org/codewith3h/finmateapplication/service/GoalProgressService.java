package org.codewith3h.finmateapplication.service;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.response.GoalProgressResponse;
import org.codewith3h.finmateapplication.entity.Goal;
import org.codewith3h.finmateapplication.entity.GoalProgress;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.GoalProgressMapper;
import org.codewith3h.finmateapplication.repository.GoalProgressRepository;
import org.codewith3h.finmateapplication.repository.GoalRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Data
@PreAuthorize("hasRole('ROLE_USER')")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GoalProgressService {

    GoalProgressRepository goalProgressRepository;
    GoalProgressMapper goalProgressMapper;
    GoalRepository goalRepository;

    public GoalProgressResponse getGoalProgressesByGoalId(int goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new AppException(ErrorCode.NO_GOAL_FOUND));

        System.err.println("test go progress goal id " + goalId);

        trackingGoalStatusAndUpdateIfNeeded(goal);

        LocalDate today = LocalDate.now();

        Optional<GoalProgress> existingProgress = goalProgressRepository.findByGoalIdAndProgressDate(goalId, today);
        GoalProgress progress;

        // if progressDate = today => update amount and percentage
        if (existingProgress.isPresent()) {
            var percent = goalProgressMapper.calculatePercentage(goal);
            log.info("Progress today found for goal id {}. Updating amount: {}, percentage {}", goalId, goal.getCurrentAmount(), percent);
            progress = existingProgress.get();
            progress.setAmount(goal.getCurrentAmount());
            progress.setPercentage(percent);
        } else {
            // else create new progress
            log.info("No progress found for today's date and goal ID {}. Creating new progress entry for today.", goalId);
            progress = new GoalProgress();
            progress.setGoal(goal);
            progress.setProgressDate(today);
            progress.setAmount(goal.getCurrentAmount());
            progress.setPercentage(goalProgressMapper.calculatePercentage(goal));
        }

        goalProgressRepository.save(progress);

        return goalProgressMapper.toGoalProgressResponse(progress);
    }

    private void trackingGoalStatusAndUpdateIfNeeded(Goal goal) {
        LocalDate today = LocalDate.now();

        String status = goal.getStatus();
        if ("COMPLETED".equals(status) || "CANCEL".equals(status) || "FAILED".equals(status)) {
            return;
        }

        boolean deadlinePassed = goal.getDeadline().isBefore(today);
        boolean goalReached = goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0;

        if (goalReached && !deadlinePassed) {
            goal.setStatus("COMPLETED");
            log.info("Goal id {} marked as COMPLETED: currentAmount={} reached targetAmount={}, deadline not passed",
                    goal.getId(), goal.getCurrentAmount(), goal.getTargetAmount());
        } else if (deadlinePassed && !goalReached) {
            goal.setStatus("FAILED");
            log.info("Goal id {} marked as FAILED: deadline passed, currentAmount={} less than targetAmount={}",
                    goal.getId(), goal.getCurrentAmount(), goal.getTargetAmount());
        } else if (deadlinePassed) {
            goal.setStatus("FAILED");
            log.info("Goal id {} marked as FAILED: deadline passed, but goalReached=true, currentAmount={}, targetAmount={}",
                    goal.getId(), goal.getCurrentAmount(), goal.getTargetAmount());
        } else {
            log.info("Goal id {} still IN_PROGRESS", goal.getId());
        }
        goalRepository.save(goal);
    }


    public Page<GoalProgressResponse> getAllGoalProgressesUniqueByDate(Integer userId, String status, int page, int size) {
        updateTodayGoalProgressList(userId);
        Pageable pageable = PageRequest.of(page, size);

        Page<GoalProgress> progressPage = goalProgressRepository.findGoalProgressesByGoal_User_IdAndGoal_StatusIsNot(userId, status, pageable);

        List<GoalProgressResponse> responses = progressPage.getContent().stream()
                .collect(Collectors.groupingBy(goalProgress ->
                                goalProgress.getGoal().getId(),
                        Collectors.maxBy(Comparator.comparing(GoalProgress::getProgressDate))
                ))
                .values()
                .stream()
                .flatMap(Optional::stream)
                .map(goalProgressMapper::toGoalProgressResponse)
                .toList();

        System.out.println("returning " + responses.size() + " progress responses");
        return new PageImpl<>(responses, PageRequest.of(page, size), progressPage.getTotalElements());
    }

    public Page<GoalProgressResponse> getListGoalProgressByGoalId(int goalId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<GoalProgress> progressPage = goalProgressRepository.findGoalProgressesByGoal_Id(goalId, pageable);

        List<GoalProgressResponse> responses = progressPage.getContent().stream()
                .map(goalProgressMapper::toGoalProgressResponse)
                .toList();
        return new PageImpl<>(responses, pageable, progressPage.getTotalElements());
    }


    private void updateTodayGoalProgressList(Integer userId) {
        List<Goal> userGoals = goalRepository.findByUserIdAndStatusIs(userId, "IN_PROGRESS");
        for (Goal goal : userGoals) {
            System.out.println("Goal id " + goal.getId() + " status " + goal.getStatus());
            ensureTodayProgressForGoal(goal);
        }
    }

    private void ensureTodayProgressForGoal(Goal goal) {
        LocalDate today = LocalDate.now();
        Optional<GoalProgress> existingProgress = goalProgressRepository.findByGoalIdAndProgressDate(goal.getId(), today);
        GoalProgress progress;

        if (existingProgress.isPresent()) {
            progress = existingProgress.get();
        } else {
            progress = new GoalProgress();
            progress.setGoal(goal);
            progress.setProgressDate(today);
        }

        progress.setAmount(goal.getCurrentAmount());
        progress.setPercentage(goalProgressMapper.calculatePercentage(goal));
        goalProgressRepository.save(progress);
    }
}