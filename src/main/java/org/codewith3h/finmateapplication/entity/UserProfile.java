package org.codewith3h.finmateapplication.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "UserProfiles")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "birthdate")
    private LocalDate birthdate;

    @Nationalized
    @Column(name = "gender", length = 10)
    private String gender;

    @Nationalized
    @Column(name = "avatar_url")
    private String avatarUrl;

    @Nationalized
    @Column(name = "phone", length = 20)
    private String phone;

    @Nationalized
    @Column(name = "address")
    private String address;

    @Nationalized
    @Column(name = "occupation", length = 100)
    private String occupation;

    @Column(name = "monthly_income", precision = 18, scale = 2)
    private BigDecimal monthlyIncome;

    @Nationalized
    @Lob
    @Column(name = "notification_preferences")
    private String notificationPreferences;

    @ColumnDefault("getdate()")
    @Column(name = "updated_at")
    private Instant updatedAt;

}