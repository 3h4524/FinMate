package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByUserId(Integer userId, Pageable pageable);

    Optional<Transaction> findByIdAndUserId(Integer transactionId,  Integer userId);

    Page<Transaction> findByUserIdAndTransactionDateBetween(Integer userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<Transaction> findByUserIdAndCategoryId(Integer userId, Integer categoryId, Pageable pageable);

    Page<Transaction> findByUserIdAndUserCategoryId(Integer userId, Integer userCategoryId, Pageable pageable);

    Page<Transaction> findByUserIdAndIsRecurring(Integer userId, boolean isRecurring, Pageable pageable);

    void deleteByUserId(Integer userId);

    Long countByUserId(Integer userId);
}
