package org.codewith3h.finmateapplication.controller;

import jakarta.validation.Valid;
import org.codewith3h.finmateapplication.dto.request.CreateBudgetRequest;
import org.codewith3h.finmateapplication.dto.request.UpdateBudgetRequest;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.BudgetAnalysisResponse;
import org.codewith3h.finmateapplication.dto.response.BudgetResponse;
import org.codewith3h.finmateapplication.service.BudgetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/budget")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(
            @Valid @RequestBody CreateBudgetRequest request) {
        BudgetResponse response = budgetService.createBudget(request);
        ApiResponse<BudgetResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(response);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> updateBudget(
            @PathVariable("id") Integer budgetId,
            @Valid @RequestBody UpdateBudgetRequest request) {
        BudgetResponse response = budgetService.updateBudget(budgetId, request);
        ApiResponse<BudgetResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(response);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBudget(
            @PathVariable("id") Integer budgetId) {
        budgetService.deleteBudget(budgetId);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Budget deleted successfully.");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Page<BudgetResponse>>> getBudgets(
            @RequestParam(name = "periodType", required = false) String periodType,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BudgetResponse> response = budgetService.getBudgets(periodType, pageable);
        ApiResponse<Page<BudgetResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(response);
        apiResponse.setCode(1000);
        apiResponse.setMessage("Success");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/analysis")
    public ResponseEntity<ApiResponse<Page<BudgetAnalysisResponse>>> getBudgetAnalysis(
            @RequestParam(name = "period", required = false) String periodType,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BudgetAnalysisResponse> response = budgetService.getBudgetAnalysis(periodType, pageable);
        ApiResponse<Page<BudgetAnalysisResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(response);
        return ResponseEntity.ok(apiResponse);
    }
}