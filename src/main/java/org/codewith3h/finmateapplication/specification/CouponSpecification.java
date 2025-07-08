package org.codewith3h.finmateapplication.specification;

import org.codewith3h.finmateapplication.entity.Coupon;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class CouponSpecification {
    public static Specification<Coupon> hasCode(String code) {
        return (root, query, cb) ->
                code == null ? null : cb.like(cb.lower(root.get("code")), "%" + code.toLowerCase() + "%");
    }

    public static Specification<Coupon> hasIsActive(Boolean isActive){
        return (root, query, cb) ->
                isActive == null ? null :  cb.equal(root.get("isActive"), isActive);
    }

    public static Specification<Coupon> hasExpiryDateBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            if (startDate == null && endDate == null)
                return cb.conjunction();
            if (startDate == null)
                return cb.lessThanOrEqualTo(root.get("expiryDate"), endDate);
            if (endDate == null)
                return cb.greaterThanOrEqualTo(root.get("expiryDate"), startDate);
            return cb.between(root.get("expiryDate"), startDate, endDate);
        };
    }
}
