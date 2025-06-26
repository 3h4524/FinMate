package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface SubcriptionRepository extends JpaRepository<Subscription, Integer> {
    Subscription findSubscriptionById(Integer id);

    Page<Subscription> findSubscriptionsByStatusAndEndDateBefore(String active, Instant now, Pageable pageable);

    boolean existsByUserIdAndStatus(Integer id, String active);
}
