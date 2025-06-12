package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.Goal;
import org.codewith3h.finmateapplication.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal,Integer> {

    List<Goal> findByUserId(Integer user_id);

    List<Goal> findGoalsByUser_IdAndStatusIsNot(Integer userId, String status);

    List<Goal> findByUserIdAndStatusIs(Integer userId, String status);

    List<Goal> findGoalByStatus(String status);

    Page<Goal> findGoalByStatusAndDeadlineBefore(String status, LocalDate deadlineBefore, Pageable pageable);

    List<Goal> findGoalByStatusAndNotificationEnabledAndCurrentAmountLessThan(String status, Boolean notificationEnabled, BigDecimal currentAmountIsLessThan);

    Page<Goal> findGoalByStatusAndNotificationEnabled(String status, Boolean notificationEnabled, Pageable pageable);
}
