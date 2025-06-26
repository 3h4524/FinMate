package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.PremiumPackage;
import org.codewith3h.finmateapplication.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
    List<Subscription> findByPremiumPackage(PremiumPackage premiumPackage);

    Page<Subscription> findSubscriptionsByStatus(String status, Pageable pageable);

    List<Subscription> findByStatus(String status);

    List<Subscription> findSubscriptionsByUser_Id(Integer userId);

    List<Subscription> findSubscriptionsByUser_IdAndStatus(Integer userId, String status);


    List<Subscription> findByPremiumPackageAndStatusIn(PremiumPackage premiumPackage, List<String> statuses);

    Subscription findSubscriptionById(Integer id);

    Page<Subscription> findSubscriptionsByStatusAndEndDateBefore(String active, Instant now, Pageable pageable);

    boolean existsByUserIdAndStatus(Integer id, String active);

}
