package org.codewith3h.finmateapplication.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "Users")
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
    private Instant lastLoginAt;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

}