package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubcriptionRepository extends JpaRepository<Subscription, Long> {
}
