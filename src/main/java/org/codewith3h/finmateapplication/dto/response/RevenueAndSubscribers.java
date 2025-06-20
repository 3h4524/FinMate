package org.codewith3h.finmateapplication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RevenueAndSubscribers {
    private Integer subscribers;
    private BigDecimal revenue;
}
