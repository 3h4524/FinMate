package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.RecurringTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction,Integer> {

    Page<RecurringTransaction> findByUserId (Integer userId, Pageable pageable);

    Optional<RecurringTransaction> findByIdAndUserId(Integer id, Integer userId);

    List<RecurringTransaction> findByIsActiveTrueAndNextDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate nextDate, LocalDate endDate);

    Page<RecurringTransaction> findByUserIdAndIsActiveTrue(Integer userId, Pageable pageable);

    @Query(value = """
    SELECT * FROM recurring_transaction 
    WHERE user_id = :userId 
      AND EXTRACT(MONTH FROM next_date) = EXTRACT(MONTH FROM CURRENT_DATE)
      AND EXTRACT(YEAR FROM next_date) = EXTRACT(YEAR FROM CURRENT_DATE)
    ORDER BY next_date ASC
    LIMIT :limit
""", nativeQuery = true)
    List<RecurringTransaction> findRecurringTransactionForUserInThisMonth(
            @Param("userId") Integer userId,
            @Param("limit") int limit);

    int countByUserId(Integer userId);
}
