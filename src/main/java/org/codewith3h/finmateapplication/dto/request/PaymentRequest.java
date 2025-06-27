package org.codewith3h.finmateapplication.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentRequest {

    @NotNull(message = "PACKAGE_ID_IS_REQUIRED")
    Integer packageId;

    @Size(max = 50, message = "Code cannot exceed 50 characters")
    String code;
}
