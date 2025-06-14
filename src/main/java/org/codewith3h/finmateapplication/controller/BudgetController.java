package org.codewith3h.finmateapplication.controller;

import org.codewith3h.finmateapplication.dto.request.CreateBudgetRequest;
import org.codewith3h.finmateapplication.dto.request.UpdateBudgetRequest;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.BudgetAnalysisResponse;
import org.codewith3h.finmateapplication.dto.response.BudgetResponse;
import org.codewith3h.finmateapplication.service.BudgetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/budget")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(
            @RequestHeader(name = "userId") Integer userId,
            @RequestBody CreateBudgetRequest request) {

        request.setUserId(userId);
        BudgetResponse response = budgetService.createBudget(request);
        ApiResponse<BudgetResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(response);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> updateBudget(
            @RequestHeader(name = "userId") Integer userId,
            @PathVariable("id") Integer budgetId,
            @RequestBody UpdateBudgetRequest request) {

        request.setUserId(userId);
        BudgetResponse response = budgetService.updateBudget(budgetId, request);
        ApiResponse<BudgetResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(response);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBudget(
            @RequestHeader(name = "userId") Integer userId,
            @PathVariable("id") Integer budgetId) {

        budgetService.deleteBudget(budgetId, userId);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Budget deleted successfully.");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Page<BudgetResponse>>> getBudgets(
            @RequestHeader(name = "userId") Integer userId,
            @RequestParam(name = "periodType", required = false) String periodType,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        System.out.println("Fetch tới get List budgets");

        Pageable pageable = PageRequest.of(page, size);
        Page<BudgetResponse> response = budgetService.getBudgets(userId, periodType, startDate, pageable);
        ApiResponse<Page<BudgetResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/analysis")
    public ResponseEntity<ApiResponse<Page<BudgetAnalysisResponse>>> getBudgetAnalysis(
            @RequestHeader(name = "userId") Integer userId,
            @RequestParam(name = "periodType", required = false) String periodType,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<BudgetAnalysisResponse> response = budgetService.getBudgetAnalysis(userId, periodType, startDate, pageable);
        ApiResponse<Page<BudgetAnalysisResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(response);
        return ResponseEntity.ok(apiResponse);
    }
}