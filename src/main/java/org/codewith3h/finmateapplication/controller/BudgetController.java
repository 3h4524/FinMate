package org.codewith3h.finmateapplication.controller;

import org.codewith3h.finmateapplication.dto.request.CreateBudgetRequest;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.BudgetAnalysisResponse;
import org.codewith3h.finmateapplication.dto.response.BudgetResponse;
import org.codewith3h.finmateapplication.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/budget")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(
            @RequestHeader(name = "userId") Integer userId,
            @RequestBody CreateBudgetRequest request) {
        if (userId == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, "Thiếu userId trong header."));
        }
        request.setUserId(userId);
        BudgetResponse response = budgetService.createBudget(request);
        ApiResponse<BudgetResponse> apiResponse = new ApiResponse();
        apiResponse.setResult(response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getBudgets(
            @RequestHeader(name = "userId") Integer userId,
            @RequestParam(name = "periodType", required = false) String periodType,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {

        System.out.println("Fetch toi get List budgets");
        if (userId == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, "Thiếu userId trong header."));
        }
        List<BudgetResponse> response = budgetService.getBudgets(userId, periodType, startDate);
        ApiResponse<List<BudgetResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/analysis")
    public ResponseEntity<ApiResponse<List<BudgetAnalysisResponse>>> getBudgetAnalysis(
            @RequestHeader(name = "userId") Integer userId,
            @RequestParam(name = "periodType", required = false) String periodType,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        if (userId == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, "Thiếu userId trong header."));
        }
        List<BudgetAnalysisResponse> response = budgetService.getBudgetAnalysis(userId, periodType, startDate);
        ApiResponse<List<BudgetAnalysisResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(response);
        return ResponseEntity.ok(apiResponse);
    }
}