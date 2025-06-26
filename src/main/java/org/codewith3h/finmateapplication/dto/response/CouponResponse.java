package org.codewith3h.finmateapplication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponResponse {
    public String code;
    public String description;
    private BigDecimal discountPercentage;
    private Integer maxUsage;
    private Integer usedCount;
    private LocalDateTime expiryDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
