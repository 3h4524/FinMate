package org.codewith3h.finmateapplication.controller;

import org.codewith3h.finmateapplication.entity.Transaction;
import org.codewith3h.finmateapplication.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import lombok.Builder;

import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
@Validated
@Builder
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<?> getTransactions(@RequestParam Integer userId) {
        log.info("Getting transactions for user ID: {}", userId);
        Map<String, Object> response = transactionService.getTransactions(userId);
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping
    public ResponseEntity<?> createTransaction(@RequestBody Transaction transaction) {
        log.info("Creating transaction for user ID: {}", transaction.getUser().getId());
        Map<String, Object> response = transactionService.createTransaction(transaction);
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getTransactionStatistics(@RequestParam Integer userId) {
        log.info("Getting transaction statistics for user ID: {}", userId);
        Map<String, Object> response = transactionService.getTransactionStatistics(userId);
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/income")
    public ResponseEntity<?> getIncome(@RequestParam Integer userId) {
        log.info("Getting income for user ID: {}", userId);
        Map<String, Object> response = transactionService.getIncome(userId);
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/expenses")
    public ResponseEntity<?> getExpenses(@RequestParam Integer userId) {
        log.info("Getting expenses for user ID: {}", userId);
        Map<String, Object> response = transactionService.getExpenses(userId);
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
} 