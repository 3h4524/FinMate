package org.codewith3h.finmateapplication.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@DynamicInsert
@Data
@Table(name = "GoalContributions")
public class GoalContribution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contribution_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Nationalized
    @Column(name = "note")
    private String note;

    @ColumnDefault("getdate()")
    @Column(name = "contribution_date", nullable = false)
    private LocalDate contributionDate;

    @ColumnDefault("getdate()")
    @Column(name = "created_at")
    private Instant createdAt;
}