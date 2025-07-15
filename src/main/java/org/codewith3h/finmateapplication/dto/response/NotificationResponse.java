package org.codewith3h.finmateapplication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private Integer userId;
    private String title;
    private String message;
    private String type;
    private String relatedEntityType;
    private String relatedEntityId;
}
