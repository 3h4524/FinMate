package org.codewith3h.finmateapplication.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PremiumPaymentResponse {
    String vnp_ResponseCode;
    String vnp_TxnRef;
}
