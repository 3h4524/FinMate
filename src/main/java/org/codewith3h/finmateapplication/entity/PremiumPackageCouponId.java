package org.codewith3h.finmateapplication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class PremiumPackageCouponId implements Serializable {
    private static final long serialVersionUID = -624957570863766389L;
    @NotNull
    @Column(name = "package_id", nullable = false)
    private Integer packageId;

    @NotNull
    @Column(name = "coupon_id", nullable = false)
    private Integer couponId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        PremiumPackageCouponId entity = (PremiumPackageCouponId) o;
        return Objects.equals(this.packageId, entity.packageId) &&
                Objects.equals(this.couponId, entity.couponId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageId, couponId);
    }

}