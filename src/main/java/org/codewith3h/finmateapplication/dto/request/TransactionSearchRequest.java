package org.codewith3h.finmateapplication.dto.request;

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
public class TransactionSearchRequest {

    private Integer userId;
    private Integer categoryId;
    private Integer userCategoryId;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String paymentMethod;
    private String location;
    private Boolean isRecurring;
    private String note;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "TransactionDate";
    private String sortDirection = "DESC";
}
