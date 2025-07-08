package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.Coupon;
import org.codewith3h.finmateapplication.entity.PremiumPackage;
import org.codewith3h.finmateapplication.entity.PremiumPackageCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PremiumPackageCouponRepository extends JpaRepository<PremiumPackageCoupon,Integer> {
    List<PremiumPackageCoupon> findByCoupon(Coupon coupon);
}
