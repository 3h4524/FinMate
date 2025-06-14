package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.Budget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Integer> {
    Page<Budget> findByUser_Id(Integer userId, Pageable pageable);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.periodType = :periodType AND b.startDate = :startDate AND b.category.id = :categoryId")
    Optional<Budget> findByUserIdAndPeriodTypeAndStartDateAndCategoryId(
            @Param("userId") Integer userId,
            @Param("periodType") String periodType,
            @Param("startDate") LocalDate startDate,
            @Param("categoryId") Integer categoryId
    );
}