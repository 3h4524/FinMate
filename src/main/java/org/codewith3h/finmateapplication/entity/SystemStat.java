package org.codewith3h.finmateapplication.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "SystemStats")
public class SystemStat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stat_id", nullable = false)
    private Integer id;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "total_users", nullable = false)
    private Integer totalUsers;

    @Column(name = "active_users", nullable = false)
    private Integer activeUsers;

    @Column(name = "premium_users", nullable = false)
    private Integer premiumUsers;

    @Column(name = "total_transactions", nullable = false)
    private Integer totalTransactions;

    @Column(name = "total_goals", nullable = false)
    private Integer totalGoals;

    @Column(name = "total_income", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalIncome;

    @ColumnDefault("getdate()")
    @Column(name = "created_at")
    private Instant createdAt;

}