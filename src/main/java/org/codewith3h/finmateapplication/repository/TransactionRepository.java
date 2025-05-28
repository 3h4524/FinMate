package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.Transaction;
import org.codewith3h.finmateapplication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserOrderByTransactionDateDesc(User user);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.amount > 0")
    BigDecimal calculateTotalIncome(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(ABS(t.amount)), 0) FROM Transaction t WHERE t.user.id = :userId AND t.amount < 0")
    BigDecimal calculateTotalExpenses(@Param("userId") Long userId);
}
