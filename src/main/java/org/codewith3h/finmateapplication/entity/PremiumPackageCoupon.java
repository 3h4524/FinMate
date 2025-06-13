package org.codewith3h.finmateapplication.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.codewith3h.finmateapplication.entity.PremiumPackage;

@Getter
@Setter
@Entity
@Table(name = "PremiumPackageCoupons")
public class PremiumPackageCoupon {
    @EmbeddedId
    private PremiumPackageCouponId id;

    @MapsId("couponId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @MapsId("packageId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id", nullable = false)
    private PremiumPackage premiumPackage;

}