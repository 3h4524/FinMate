package org.codewith3h.finmateapplication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponResponse {
    private Integer id;
    private String code;
    private String description;
    private BigDecimal discountPercentage;
    private Integer maxUsage;
    private Integer usedCount;
    private LocalDate expiryDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private List<PremiumPackageFetchResponse> premiumPackages;
}
