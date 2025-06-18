package org.codewith3h.finmateapplication.dto.request;

import jakarta.validation.constraints.Min;
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
    @NotNull(message = "PREMIUM_PACKAGE_NOT_FOUND")
    Integer packageId;

    @NotNull(message = "USER_NOT_FOUND")
    Integer userId;

    @Min(value = 1, message = "AMOUNT_MUST_BE_POSITIVE")
    Integer amount;
}
