package org.codewith3h.finmateapplication.service;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
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

        LocalDate today = LocalDate.now();

        Optional<GoalProgress> existingProgress = goalProgressRepository.findByGoalIdAndProgressDate(goalId, today);
        GoalProgress progress;

        if (existingProgress.isPresent()) {
            progress = existingProgress.get();
            progress.setAmount(goal.getCurrentAmount());
            progress.setPercentage(goalProgressMapper.calculatePercentage(goal));
        } else {
            progress = new GoalProgress();
            progress.setGoal(goal);
            progress.setProgressDate(today);
            progress.setAmount(goal.getCurrentAmount());
            progress.setPercentage(goalProgressMapper.calculatePercentage(goal));
        }

        goalProgressRepository.save(progress);

        return goalProgressMapper.toGoalProgressResponse(progress);
    }

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