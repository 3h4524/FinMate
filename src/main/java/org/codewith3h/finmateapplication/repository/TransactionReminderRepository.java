package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.TransactionReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionReminderRepository extends JpaRepository<TransactionReminder,Integer> {
    Optional<TransactionReminder> findByTokenAndIsUsedFalse(String token);
}
