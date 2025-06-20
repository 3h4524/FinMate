package org.codewith3h.finmateapplication.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "Categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", nullable = false)
    private Integer id;

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

    @ColumnDefault("0")
    @Column(name = "is_system")
    private Boolean isSystem;

    @ColumnDefault("getdate()")
    @Column(name = "created_at")
    private Instant createdAt;

}