package org.codewith3h.finmateapplication.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponSearchRequest {
    private String code;
    private Boolean isActive;
    private LocalDate StartDate;
    private LocalDate endDate;
    private Integer page = 0;
    private Integer size = 6;
    private String sortBy = "usedCount";
    private String sortDirection = "DESC";
}
