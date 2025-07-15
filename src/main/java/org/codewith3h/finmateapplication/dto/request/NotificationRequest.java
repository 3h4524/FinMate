package org.codewith3h.finmateapplication.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationRequest {
    private Integer userId;
    private String title;
    private String message;
    private String type;
    private String relatedEntityType;
    private Integer relatedEntityId;
}
