package org.codewith3h.finmateapplication.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionReminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @Size(max = 100)
    @NotNull
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Size(max = 40)
    @NotNull
    @Column(name = "token", nullable = false, length = 40)
    private String token;

    @NotNull
    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}