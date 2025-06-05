package org.codewith3h.finmateapplication.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
public class LoginHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Nationalized
    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType;

    @Nationalized
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Nationalized
    @Column(name = "device_info")
    private String deviceInfo;

    @ColumnDefault("getdate()")
    @Column(name = "created_at")
    private Instant createdAt;

}