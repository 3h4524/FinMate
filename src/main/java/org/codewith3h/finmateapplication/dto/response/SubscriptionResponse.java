package org.codewith3h.finmateapplication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubscriptionResponse {
    private String userName;
    private int packageId;
    private String packageName;
    private LocalDate createdAt;
    private String status;
    private BigDecimal amount;
    private LocalDate endDate;
}
