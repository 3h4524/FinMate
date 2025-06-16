package org.codewith3h.finmateapplication.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.codewith3h.finmateapplication.dto.request.PremiumPaymentRequest;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.PremiumPaymentResponse;
import org.codewith3h.finmateapplication.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class VNPayController {

    PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> initiatePayment(
            @RequestBody @Valid PremiumPaymentRequest request, HttpServletRequest httpServletRequest) {
        String url = paymentService.createPayment(request, httpServletRequest);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .message("Payment initiated successfully")
                .code(1000)
                .result(url)
                .build());
    }

    @PostMapping("/return")
    public ResponseEntity<ApiResponse<Boolean>> handlePaymentReturn(
            @RequestBody PremiumPaymentResponse response,
            HttpServletRequest httpServletRequest
    ) {
        boolean isSuccess = paymentService.handlePaymentReturn(response, httpServletRequest);

        return ResponseEntity.ok(ApiResponse
                .<Boolean>builder()
                .result(isSuccess)
                .code(1000)
                .build());
    }
}
