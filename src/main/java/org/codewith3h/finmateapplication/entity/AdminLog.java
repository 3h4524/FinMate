package org.codewith3h.finmateapplication.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "AdminLogs")
public class AdminLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @Nationalized
    @Column(name = "\"action\"", nullable = false)
    private String action;

    @Nationalized
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Integer entityId;

    @Nationalized
    @Lob
    @Column(name = "details")
    private String details;

    @Nationalized
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @ColumnDefault("getdate()")
    @Column(name = "created_at")
    private Instant createdAt;

}