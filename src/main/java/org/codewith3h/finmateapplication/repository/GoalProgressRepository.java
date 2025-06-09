package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.GoalProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GoalProgressRepository extends JpaRepository<GoalProgress, Integer> {
    @Query("SELECT gp FROM GoalProgress gp WHERE gp.goal.id = :goalId AND gp.progressDate = :date")
    Optional<GoalProgress> findByGoalIdAndProgressDate(@Param("goalId") int goalId, @Param("date") LocalDate date);

    List<GoalProgress> findByGoal_User_Id(Integer userId);


    List<GoalProgress> findGoalProgressesByGoal_Id(Integer goalId);
}