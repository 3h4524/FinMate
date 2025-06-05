package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Integer> {
    List<Budget> findByUser_Id(Integer userId);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.periodType = :periodType AND b.startDate = :startDate")
    Optional<Budget> findByUserIdAndPeriodTypeAndStartDate(
            @Param("userId") Integer userId,
            @Param("periodType") String periodType,
            @Param("startDate") LocalDate startDate
    );
}