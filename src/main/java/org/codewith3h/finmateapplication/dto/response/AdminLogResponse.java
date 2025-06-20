package org.codewith3h.finmateapplication.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class AdminLogResponse {
    private Integer id;
    private Integer adminId;
    private String action;
    private String entityType;
    private Integer entityId;
    private String details;
    private Instant createdAt;
}