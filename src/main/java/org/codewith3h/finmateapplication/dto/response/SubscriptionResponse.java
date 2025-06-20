package org.codewith3h.finmateapplication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubscriptionResponse {
    private String userName;
    private String packageName;
    private LocalDateTime createdAt;
    private String status;
    private BigDecimal amount;
}
