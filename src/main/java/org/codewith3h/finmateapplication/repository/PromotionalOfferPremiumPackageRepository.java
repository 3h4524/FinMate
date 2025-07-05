package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.PromotionalOfferPremiumPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface PromotionalOfferPremiumPackageRepository extends JpaRepository<PromotionalOfferPremiumPackage, Integer> {
    Set<PromotionalOfferPremiumPackage> findPromotionalOfferPremiumPackagesByPackageFieldId(Integer packageFieldId);
}
