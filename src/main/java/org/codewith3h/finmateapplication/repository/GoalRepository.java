package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.Goal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal,Integer> {

    List<Goal> findByUserIdAndStatusIs(Integer userId, String status);

    Page<Goal> findGoalByStatusAndDeadlineBefore(String status, LocalDate deadlineBefore, Pageable pageable);

    Page<Goal> findGoalByStatusAndNotificationEnabled(String status, Boolean notificationEnabled, Pageable pageable);

    int countGoalsByUser_IdAndStatus(Integer userId, String status);

    @Query(value = "SELECT * FROM GOALS WHERE user_id = :userId AND status = :status ORDER BY Deadline", nativeQuery = true)
    List<Goal> findByUserIdAndStatusOrderByDeadline(@Param("userId") Integer userId, @Param("status") String status);
}
