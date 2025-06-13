package org.codewith3h.finmateapplication.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PremiumPackageRequestDTO {
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPercentage;
    private Integer durationDays;
    private String features;
    private Boolean isActive;
}
