package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.Goal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal,Integer> {

    List<Goal> findByUserIdAndStatusIs(Integer userId, String status);

    Page<Goal> findGoalByStatusAndDeadlineBefore(String status, LocalDate deadlineBefore, Pageable pageable);

    Page<Goal> findGoalByStatusAndNotificationEnabled(String status, Boolean notificationEnabled, Pageable pageable);

    Integer countGoalsByUser_Id(Integer userId);
}
