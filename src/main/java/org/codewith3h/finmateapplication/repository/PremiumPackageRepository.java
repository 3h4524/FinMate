package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.PremiumPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PremiumPackageRepository extends JpaRepository<PremiumPackage, Integer> {

    PremiumPackage findPremiumPackageById(Integer packageId);
}
