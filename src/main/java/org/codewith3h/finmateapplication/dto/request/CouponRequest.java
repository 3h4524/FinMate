package org.codewith3h.finmateapplication.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponRequest {
    @NotNull(message = "code is required")
    @Size(max = 50, message = "code cannot exceed 50 characters")
    private String code;
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
    @NotNull(message = "Discount percentage is required")
    @DecimalMin(value = "0", inclusive = false, message = "Discount percentage must be greater than 0")
    private BigDecimal discountPercentage;
    @DecimalMin(value = "0", inclusive = false, message = "Max usage must be greater than 0")
    private Integer maxUsage;
    @NotNull(message = "Expiry Date is required")
    private LocalDate expiryDate;
    private Boolean isActive;
}
