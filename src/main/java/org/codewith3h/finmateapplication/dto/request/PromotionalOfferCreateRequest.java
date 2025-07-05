package org.codewith3h.finmateapplication.dto.request;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PromotionalOfferCreateRequest {

    @NotNull
    String discountEvent;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Discount percent must be greater than 0")
    BigDecimal discountPercentage;

    @NotNull
    LocalDate startDate;

    @NotNull
    LocalDate expiryDate;

    Set<Integer> packageIds;

}
