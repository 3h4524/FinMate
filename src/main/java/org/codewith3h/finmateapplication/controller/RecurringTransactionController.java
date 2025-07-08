package org.codewith3h.finmateapplication.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.RecurringTransactionRequest;
import org.codewith3h.finmateapplication.dto.request.TransactionCreationRequest;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.RecurringTransactionResponse;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.repository.RecurringTransactionRepository;
import org.codewith3h.finmateapplication.service.RecurringTransactionService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/recurringTransactions")
@RequiredArgsConstructor
@Builder
@Slf4j
@Validated
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

    @RequestMapping(value = "/confirm-reminder", method = {RequestMethod.POST, RequestMethod.GET})
    public RedirectView confirmReminder(@RequestParam String token) {
        log.info("in");
        try {
            recurringTransactionService.confirmRecurringTransactionReminder(token);
            return new RedirectView("http://127.0.0.1:5500/pages/reminder-confirmed/?status=success");
        } catch (AppException ex) {
            String errorMessage = URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
            return new RedirectView("https://127.0.0.1:5500/pages/reminder-confirmed/?status=error&message=" + errorMessage);
        }
    }

    @PutMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> updateTransaction(
            @PathVariable Integer transactionId,
            @RequestBody RecurringTransactionRequest dto,
            @RequestParam Integer userId
    ){
        log.info("Updating recurring transaction");
        RecurringTransactionResponse transactionResponse = recurringTransactionService.updateRecurringTransaction(transactionId, userId,dto);

        ApiResponse<RecurringTransactionResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Recurring transaction updated");
        apiResponse.setResult(transactionResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteRecurringTransaction(
            @PathVariable Integer transactionId,
            @RequestParam Integer userId
    ) {
        log.info("deleting recurring transaction {} for user {}.", transactionId, userId);
        recurringTransactionService.deleteRecurringTransaction(transactionId, userId);

        log.info("Recurring transaction deleted successfully.");
        return ResponseEntity.noContent().build();
    }


    @GetMapping()
    public ResponseEntity<ApiResponse<Page<RecurringTransactionResponse>>> getRecurringTransactions(
            @RequestParam Integer userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "5") @Min(1) int size,
            @RequestParam(defaultValue = "nextDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.info("Fetching recurring transactions for user {}", userId);
        Page<RecurringTransactionResponse> responsePage = recurringTransactionService
                .getRecurringTransactions(userId, page, size, sortBy, sortDirection);

        ApiResponse<Page<RecurringTransactionResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Recurring transactions fetched");
        apiResponse.setResult(responsePage);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> getRecurringTransaction(
            @PathVariable Integer transactionId,
            @RequestParam Integer userId
    ) {
        log.info("Fetching recurring transaction {} for user {}", transactionId, userId);
        RecurringTransactionResponse recurringTransactionResponse = recurringTransactionService
                .getRecurringTransactionById(transactionId, userId);

        ApiResponse<RecurringTransactionResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Recurring transaction fetched");
        apiResponse.setResult(recurringTransactionResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<RecurringTransactionResponse>>> getRecurringTransactionInThisMonth(
            @RequestParam Integer userId,
            @RequestParam(defaultValue = "5") @Min(1) int limit
    ) {
        log.info("Fetching recurring transactions for user {}", userId);
        List<RecurringTransactionResponse> recurringTransactionResponses = recurringTransactionService
                .getRecurringTransactionInMonth(userId, limit);

        ApiResponse<List<RecurringTransactionResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Recurring transactions fetched");
        apiResponse.setResult(recurringTransactionResponses);
        return ResponseEntity.ok(apiResponse);
    }
}
