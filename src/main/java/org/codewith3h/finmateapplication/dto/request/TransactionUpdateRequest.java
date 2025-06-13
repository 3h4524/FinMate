package org.codewith3h.finmateapplication.dto.request;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
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
public class TransactionUpdateRequest {

    private Integer categoryId;
    private Integer userCategoryId;

    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Size(max = 255, message = "Note cannot exceed 255 characters")
    private String note;

    private LocalDate transactionDate;

    @Size(max = 255, message = "Payment method cannot exceed 255 characters")
    private String paymentMethod;

    @Size(max = 255, message = "Location cannot exceed 255 characters")
    private String location;

    @Size(max = 255, message = "Image URL cannot exceed 255 characters")
    private String imageUrl;

    private boolean isRecurring;

    @Size(max = 50, message = "Recurring pattern cannot exceed 50 characters")
    private String recurringPattern;
}
