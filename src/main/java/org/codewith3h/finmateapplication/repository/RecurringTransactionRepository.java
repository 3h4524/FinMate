package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.RecurringTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction,Integer> {

    Page<RecurringTransaction> findByUserId (Integer userId, Pageable pageable);

    Optional<RecurringTransaction> findByUserIdAndId(Integer userId, Integer id);
}
