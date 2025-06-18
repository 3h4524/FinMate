package org.codewith3h.finmateapplication.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.codewith3h.finmateapplication.enums.DurationType;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

        @Column(name = "price", nullable = false, precision = 18, scale = 2)
        private BigDecimal price;

        @Column(name = "discount_percentage", precision = 5, scale = 2)
        private BigDecimal discountPercentage;

        @ColumnDefault("1")
        @Column(name = "is_active")
        private Boolean isActive = true;

        @ColumnDefault("getdate()")
        @Column(name = "created_at")
        private LocalDateTime createdAt;

        @ColumnDefault("getdate()")
        @Column(name = "updated_at")
        private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "duration_type", nullable = false)
    private DurationType durationType;

    @Column(name = "duration_value", nullable = false)
    private Integer durationValue;


    @ManyToMany
        @JoinTable(
                name = "PremiumPackageFeatures",
                joinColumns = @JoinColumn(name = "package_id"),
                inverseJoinColumns = @JoinColumn(name = "feature_id")
        )
        private List<Feature> features = new ArrayList<>();


        @PrePersist
        public void prePersist(){
            createdAt = LocalDateTime.now();
            updatedAt = LocalDateTime.now();
        }

        @PreUpdate
        public void preUpdate(){
            updatedAt = LocalDateTime.now();
        }
    }