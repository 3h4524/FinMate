package org.codewith3h.finmateapplication.controller;

import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.TransactionResponse;
import org.codewith3h.finmateapplication.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/financial-report")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FinancialReportController {

    TransactionService transactionService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getUserTransactions(
            @RequestParam Integer userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        Page<TransactionResponse> responses = transactionService.getUserTransactions(userId, page, size, sortBy, sortDirection);
        ApiResponse<Page<TransactionResponse>> apiResponse = new  ApiResponse<>();
        apiResponse.setMessage("Transactions fetched successfully");
        apiResponse.setResult(responses);
        return ResponseEntity.ok(apiResponse);
    }
}
