package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
            "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate")
    List<Transaction> findByUserIdAndCategoryIdAndDateRange(
            @Param("userId") Integer userId,
            @Param("categoryId") Integer categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}