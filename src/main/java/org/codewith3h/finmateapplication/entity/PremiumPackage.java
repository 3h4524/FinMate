package org.codewith3h.finmateapplication.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;

    @Getter
    @Setter
    @Entity
    @Table(name = "PremiumPackages")
    public class PremiumPackage {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "package_id", nullable = false)
        private Integer id;

        @Nationalized
        @Column(name = "name", nullable = false, length = 100)
        private String name;

        @Nationalized
        @Lob
        @Column(name = "description")
        private String description;

        @Column(name = "price", nullable = false)
        private Integer price;

        @Column(name = "discount_percentage", precision = 5, scale = 2)
        private BigDecimal discountPercentage;

        @Column(name = "duration_days", nullable = false)
        private Integer durationDays;

        @Nationalized
        @Lob
        @Column(name = "features")
        private String features;

        @ColumnDefault("1")
        @Column(name = "is_active")
        private Boolean isActive;

        @ColumnDefault("getdate()")
        @Column(name = "created_at")
        private Instant createdAt;

        @ColumnDefault("getdate()")
        @Column(name = "updated_at")
        private Instant updatedAt;

    }