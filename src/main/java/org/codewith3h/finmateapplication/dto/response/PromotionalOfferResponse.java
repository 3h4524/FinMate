package org.codewith3h.finmateapplication.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class PromotionalOfferResponse {
    private Integer id;
    private String discountEvent;
    private BigDecimal discountPercentage;
    private LocalDate startDate;
    private LocalDate expiryDate;
    private Set<Integer> packageIds;
}

