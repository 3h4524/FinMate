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
@Table(name = "Notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Nationalized
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Nationalized
    @Column(name = "message", nullable = false)
    private String message;

    @Nationalized
    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @ColumnDefault("0")
    @Column(name = "is_read")
    private Boolean isRead;

    @Nationalized
    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType;

    @Column(name = "related_entity_id")
    private Integer relatedEntityId;

    @ColumnDefault("getdate()")
    @Column(name = "created_at")
    private Instant createdAt;

}