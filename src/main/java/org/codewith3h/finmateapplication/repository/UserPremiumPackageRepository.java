package org.codewith3h.finmateapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPremiumPackageRepository extends JpaRepository<UserPremiumPackage, Integer> {

    UserPremiumPackage findUserPremiumPackageById(Integer id);
}
