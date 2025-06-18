package org.codewith3h.finmateapplication.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "UserCategories")
public class UserCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_category_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Nationalized
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Nationalized
    @Column(name = "type", nullable = false, length = 10)
    private String type;

    @Nationalized
    @Column(name = "icon", length = 50)
    private String icon;

    @Nationalized
    @Column(name = "color", length = 20)
    private String color;

    @ColumnDefault("getdate()")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ColumnDefault("getdate()")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}