package org.codewith3h.finmateapplication.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.codewith3h.finmateapplication.dto.request.PaymentRequest;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.PaymentResponse;
import org.codewith3h.finmateapplication.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class PaymentController {

    PaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<String>> createPaymentLink(@RequestBody @Valid PaymentRequest request) {
        String url = paymentService.createPaymentLink(request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .message("Payment initiated successfully")
                .code(1000)
                .result(url)
                .build());
    }

    @PostMapping("/return")
    public ResponseEntity<ApiResponse<Boolean>> handlePaymentReturn(@RequestBody PaymentResponse response) {
        boolean isSuccess = paymentService.handlePaymentReturn(response);
        return ResponseEntity.ok(ApiResponse
                .<Boolean>builder()
                .result(isSuccess)
                .code(1000)
                .build());
    }

}