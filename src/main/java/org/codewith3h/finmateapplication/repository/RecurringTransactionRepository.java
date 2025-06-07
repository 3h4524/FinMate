package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction,Long> {
}
