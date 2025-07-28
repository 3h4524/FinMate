package org.codewith3h.finmateapplication.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@DynamicInsert
public class ModelTrainingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @Nationalized
    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "mse")
    private Double mse;

    @Column(name = "training_duration_seconds")
    private Double trainingDurationSeconds;

    @Column(name = "num_transactions")
    private Integer numTransactions;

    @Column(name = "training_timestamp")
    private LocalDateTime trainingTimestamp;

}