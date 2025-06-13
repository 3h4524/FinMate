package org.codewith3h.finmateapplication.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "EmailVerifications")
public class EmailVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id")
    private User user;

    @Size(max = 100)
    @NotNull
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Size(max = 6)
    @NotNull
    @Column(name = "verification_code", nullable = false, length = 6)
    private String verificationCode;

    @NotNull
    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "verified", nullable = false)
    private Boolean verified = false;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}