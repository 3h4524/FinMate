package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

<<<<<<< HEAD
import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
=======
import java.time.LocalDate;
>>>>>>> origin/authentication
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    Page<Transaction> findByUserId(Integer userId, Pageable pageable);

    Optional<Transaction> findByIdAndUserId(Integer transactionId, Integer userId);

<<<<<<< HEAD
    Page<Transaction> findByUserIdAndTransactionDateBetween(Integer userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
=======
    Page<Transaction> findByUserIdAndTransactionDateBetween(Integer userId, LocalDate startDate, LocalDate endDate, Pageable pageable);
>>>>>>> origin/authentication

    Page<Transaction> findByUserIdAndCategoryId(Integer userId, Integer categoryId, Pageable pageable);

    Page<Transaction> findByUserIdAndUserCategoryId(Integer userId, Integer userCategoryId, Pageable pageable);

    Page<Transaction> findByUserIdAndIsRecurring(Integer userId, boolean isRecurring, Pageable pageable);

    void deleteByUserId(Integer userId);

    Long countByUserId(Integer userId);

<<<<<<< HEAD
    List<Transaction> findAll(Specification<Transaction> spec);

=======
>>>>>>> origin/authentication
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
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
            "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate")
    List<Transaction> findByUserIdAndCategoryIdAndDateRange(
            @Param("userId") Integer userId,
            @Param("categoryId") Integer categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
<<<<<<< HEAD

    Optional<Transaction> findOne(Specification<Transaction> spec);

    @Query("""
        SELECT CASE WHEN COUNT(t) > 0 THEN TRUE ELSE FALSE END
        FROM Transaction t
        WHERE t.user.id = :userId
          AND t.amount = :amount
          AND t.createdAt BETWEEN :from AND :to
          AND (
               (:categoryId IS NOT NULL AND t.category.id = :categoryId)
            OR (:userCategoryId IS NOT NULL AND t.userCategory.id = :userCategoryId)
          )
    """)
    boolean existsByUserIdAndAmountAndCreatedAtBetweenAndCategoryOrUserCategory(
                @Param("userId") Integer userId,
                @Param("amount") BigDecimal amount,
                @Param("from") LocalDateTime from,
                @Param("to") LocalDateTime to,
                @Param("categoryId") Integer categoryId,
                @Param("userCategoryId") Integer userCategoryId
                );
=======
>>>>>>> origin/authentication
}
