package org.codewith3h.finmateapplication.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.EntityResolver;
import org.codewith3h.finmateapplication.dto.request.TransactionCreationRequest;
import org.codewith3h.finmateapplication.dto.request.TransactionSearchRequest;
import org.codewith3h.finmateapplication.dto.request.TransactionUpdateRequest;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.TransactionResponse;
import org.codewith3h.finmateapplication.entity.Transaction;
import org.codewith3h.finmateapplication.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
@Validated
@Builder
public class TransactionController {
    private final TransactionService transactionService;
    private final RestClient.Builder builder;

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @Valid @RequestBody TransactionCreationRequest requestDto) {

        log.info("Creating transaction for user: {}", requestDto.getUserId());

        TransactionResponse transactionResponse = transactionService.createTransaction(requestDto);
        ApiResponse<TransactionResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Transaction created successfully.");
        apiResponse.setResult(transactionResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransaction(
            @PathVariable @Positive Integer  transactionId,
            @RequestParam @Positive Integer userId,
            @Valid @RequestBody TransactionUpdateRequest requestDto
    ) {
        log.info("Updating transaction {} for user: {}", transactionId, userId);
        TransactionResponse transactionResponse = transactionService.updateTransaction(transactionId,  userId, requestDto);
        ApiResponse<TransactionResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Transaction updated successfully");
        apiResponse.setResult(transactionResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(
            @PathVariable @Positive Integer transactionId,
            @RequestParam @Positive Integer userId) {
        log.info("Fetching transaction {} for user {}", transactionId, userId);
        TransactionResponse transactionResponse = transactionService.getTransaction(transactionId, userId);
        ApiResponse<TransactionResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Transaction fetched successfully");
        apiResponse.setResult(transactionResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable @Positive Integer transactionId,
            @RequestParam @Positive Integer userId
    ) {
        log.info("Deleting transaction {} for user {}", transactionId, userId);
        transactionService.deleteTransaction(transactionId, userId);
        return ResponseEntity.noContent().build();
    }

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

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> searchTransactions(
            @Valid @RequestBody TransactionSearchRequest dto) {
        log.info("Searching transactions with criteria for user: {}", dto.getUserId());

        Page<TransactionResponse> response = transactionService.searchTransaction(dto);
        ApiResponse<Page<TransactionResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Transactions fetched successfully");
        apiResponse.setResult(response);
        return ResponseEntity.ok(apiResponse);
    }
}
