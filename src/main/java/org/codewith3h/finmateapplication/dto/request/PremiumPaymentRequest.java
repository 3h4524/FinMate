package org.codewith3h.finmateapplication.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PremiumPaymentRequest {
    @NotNull(message = "Package is not found")
    Integer packageId;

    @NotNull(message = "User is not found")
    Integer userId;

    String bankCode;

    String language;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Target amount must be greater than 0")
    Double amount;


}
