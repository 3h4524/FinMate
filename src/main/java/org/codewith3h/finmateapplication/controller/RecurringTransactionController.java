package org.codewith3h.finmateapplication.controller;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.RecurringTransactionRequest;
import org.codewith3h.finmateapplication.dto.request.TransactionCreationRequest;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.RecurringTransactionResponse;
import org.codewith3h.finmateapplication.repository.RecurringTransactionRepository;
import org.codewith3h.finmateapplication.service.RecurringTransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recurringTransactions")
@RequiredArgsConstructor
@Builder
@Slf4j
public class RecurringTransactionController {

    private final RecurringTransactionService  recurringTransactionService;

    @PostMapping
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> createTransaction(@Valid @RequestBody RecurringTransactionRequest requestDto) {

        log.info("Creating recurring transaction for user: {}", requestDto.getUserId());
        RecurringTransactionResponse response = recurringTransactionService.createRecurringTransaction(requestDto);
        ApiResponse<RecurringTransactionResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Recurring transaction created");
        apiResponse.setResult(response);

        return ResponseEntity.ok(apiResponse);
    }


    @PostMapping("/confirm-recurring")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> confirmRecurringTransactionReminder(
            @RequestParam String token) {
        RecurringTransactionResponse response = recurringTransactionService.confirmRecurringTransactionReminder(token);
        ApiResponse<RecurringTransactionResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Recurring transaction confirmed");
        apiResponse.setResult(response);
        return ResponseEntity.ok(apiResponse);
    }
}
