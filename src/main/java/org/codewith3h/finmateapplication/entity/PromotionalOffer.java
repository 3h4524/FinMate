package org.codewith3h.finmateapplication.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Entity
@DynamicInsert
@Table(name = "PromotionalOffers")
public class PromotionalOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "offer_id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "discount_event")
    private String discountEvent;

    @NotNull
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @ColumnDefault("getdate()")
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @ColumnDefault("1")
    @Column(name = "is_active")
    private Boolean isActive;

    @ColumnDefault("getdate()")
    @Column(name = "created_at")
    private LocalDate createdAt;

    @ColumnDefault("getdate()")
    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @ManyToMany
    @JoinTable(
            name = "PromotionalOfferPremiumPackage",
            joinColumns = @JoinColumn(name = "offer_id"),
            inverseJoinColumns = @JoinColumn(name = "package_id"),
            uniqueConstraints = {
                    @UniqueConstraint(columnNames = {"offer_id", "package_id"})
            }
    )
    private Set<PremiumPackage> premiumPackages;

}
