package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.PremiumPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PremiumPackageRepository extends JpaRepository<PremiumPackage,Long> {
}
