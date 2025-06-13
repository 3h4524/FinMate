package org.codewith3h.finmateapplication.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;


@Entity
@Table(name = "Transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_category_id")
    private UserCategory userCategory;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Nationalized
    @Column(name = "note")
    private String note;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Nationalized
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Nationalized
    @Column(name = "location")
    private String location;

    @Nationalized
    @Column(name = "image_url")
    private String imageUrl;

    @ColumnDefault("0")
    @Column(name = "is_recurring")
    private Boolean isRecurring;

    @Nationalized
    @Column(name = "recurring_pattern", length = 50)
    private String recurringPattern;

    @ColumnDefault("getdate()")
    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("getdate()")
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}