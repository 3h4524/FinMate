package org.codewith3h.finmateapplication.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.codewith3h.finmateapplication.enums.DurationType;
import org.codewith3h.finmateapplication.enums.FeatureCode;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PremiumPackageCreationDto {
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    @NotNull(message = "Name is required")
    String name;
    String description;
    @Min(value = 1, message = "price must be greater than 0")
    Integer price;
    @DecimalMin(value = "0.0", inclusive = false, message = "discount percent must be greater than 0")
    BigDecimal discountPercentage;
    @NotNull(message = "duration value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "duration must be greater than 0")
    Integer durationValue;

    @NotNull(message = "duration type is required")
    DurationType durationType;
    List<FeatureCode> features;
}
