package org.codewith3h.finmateapplication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Entity
@Table(name = "Users")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Nationalized
    @Column(name = "email", nullable = false)
    private String email;

    @Nationalized
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @ColumnDefault("0")
    @Column(name = "is_premium")
    private Boolean isPremium;

    @Nationalized
    @ColumnDefault("'USER'")
    @Column(name = "role", length = 20)
    private String role;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @ColumnDefault("getdate()")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ColumnDefault("getdate()")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "token")
    private String token;

    @ColumnDefault("0")
    @Column(name = "is_verified")
    private Boolean verified;

    @ColumnDefault("1")
    @Column(name = "is_new_user")
    private Boolean isNewUser;

    @ColumnDefault("0")
    @Column(name = "resend_attempts", nullable = false)
    private Integer resendAttempts;

    @Column(name = "resend_lockout_until")
    private LocalDateTime resendLockoutUntil;

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "verification_code_expiry")
    private LocalDateTime verificationCodeExpiry;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_token_expiry")
    private LocalDateTime passwordResetTokenExpiry;

    @Column(name = "isDelete")
    private Boolean isDelete;
}