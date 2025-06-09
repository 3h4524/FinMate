package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    Page<Transaction> findByUserId(Integer userId, Pageable pageable);

    Optional<Transaction> findByIdAndUserId(Integer transactionId,  Integer userId);

    Page<Transaction> findByUserIdAndTransactionDateBetween(Integer userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<Transaction> findByUserIdAndCategoryId(Integer userId, Integer categoryId, Pageable pageable);

    Page<Transaction> findByUserIdAndUserCategoryId(Integer userId, Integer userCategoryId, Pageable pageable);

    Page<Transaction> findByUserIdAndIsRecurring(Integer userId, boolean isRecurring, Pageable pageable);

    void deleteByUserId(Integer userId);

    Long countByUserId(Integer userId);

    Page<Transaction> findAll(Specification<Transaction> spec, Pageable pageable);

    @Query("SELECT YEAR(t.transactionDate), MONTH(t.transactionDate), SUM(t.amount) " +
            "FROM Transaction t WHERE t.user.id = :userId " +
            "GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate) " +
            "ORDER BY YEAR(t.transactionDate) DESC, MONTH(t.transactionDate) DESC")
    List<Object[]> getMonthlySummaryByUser(@Param("userId") Integer userId);

    @Query("SELECT COALESCE(t.category, t.userCategory), SUM(t.amount), COUNT(t) " +
            "FROM Transaction t WHERE t.user.id = :userId " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY COALESCE(t.category, t.userCategory)")
    List<Object[]> getCategorySummaryByUser(
            @Param("userId") Integer userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

}
