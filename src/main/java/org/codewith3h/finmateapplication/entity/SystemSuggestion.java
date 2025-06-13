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
@Table(name = "SystemSuggestions")
public class SystemSuggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "suggestion_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Nationalized
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Nationalized
    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Nationalized
    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @ColumnDefault("0")
    @Column(name = "is_read")
    private Boolean isRead;

    @Column(name = "is_helpful")
    private Boolean isHelpful;

    @ColumnDefault("getdate()")
    @Column(name = "suggested_at")
    private Instant suggestedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

}