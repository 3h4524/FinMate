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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GoalProgressService {

    GoalProgressRepository goalProgressRepository;
    GoalProgressMapper goalProgressMapper;
    GoalRepository goalRepository;

    public GoalProgressResponse getGoalProgressesByGoalId(int goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new AppException(ErrorCode.NO_GOAL_FOUND));

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


    // This function is for testing purposes only and is not part of the final implementation. Hehe
    public List<GoalProgressResponse> getAllGoalProgressesUniqueByDate(Integer userId, String filter) {

        List<Goal> userGoals = goalRepository.findByUserId(userId);
        for (Goal goal : userGoals) {
            ensureTodayProgressForGoal(goal);
        }

        List<GoalProgress> allProgresses = goalProgressRepository.findByGoal_User_Id(userId);
        Map<Integer, GoalProgress> latestProgressByGoalId = new HashMap<>();
        List<GoalProgressResponse> responses = new ArrayList<>();

        LocalDate now = LocalDate.now();

        for (GoalProgress progress : allProgresses) {
            LocalDate progressDate = progress.getProgressDate();
            int goalId = progress.getGoal().getId();

            // Kiểm tra filter
            boolean isValid = false;
            if (filter == null || filter.isEmpty()) {
                isValid = true;
            } else if ("weekly".equalsIgnoreCase(filter)) {
                // Cùng tuần năm
                isValid = isSameWeek(progressDate, now);
            } else if ("monthly".equalsIgnoreCase(filter)) {
                isValid = progressDate.getMonth() == now.getMonth()
                        && progressDate.getYear() == now.getYear();
            } else if ("yearly".equalsIgnoreCase(filter)) {
                isValid = progressDate.getYear() == now.getYear();
            }

            // Nếu progress nằm trong khoảng thời gian phù hợp
            if (isValid) {
                GoalProgress existing = latestProgressByGoalId.get(goalId);
                if (existing == null || progressDate.isAfter(existing.getProgressDate())) {
                    latestProgressByGoalId.put(goalId, progress);
                }
            }
        }

        for (GoalProgress progress : latestProgressByGoalId.values()) {
            responses.add(goalProgressMapper.toGoalProgressResponse(progress));
        }

        return responses;
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

    private boolean isSameWeek(LocalDate date1, LocalDate date2) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        return date1.get(weekFields.weekOfYear()) == date2.get(weekFields.weekOfYear())
                && date1.getYear() == date2.getYear();
    }


    public List<GoalProgressResponse> getListGoalProgressByGoalId(int goalId) {
        return goalProgressRepository.findGoalProgressesByGoal_Id(goalId).stream().map(goalProgressMapper::toGoalProgressResponse).collect(Collectors.toList());
    }
}