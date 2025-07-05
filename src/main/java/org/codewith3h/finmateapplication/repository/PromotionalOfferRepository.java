package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.PromotionalOffer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Set;

public interface PromotionalOfferRepository extends JpaRepository<PromotionalOffer, Integer> {
    Set<PromotionalOffer> findByIsActiveAndStartDateLessThanEqualAndExpiryDateGreaterThanEqual(Boolean isActive, LocalDate startDateIsGreaterThan, LocalDate expiryDateIsGreaterThan);
}
