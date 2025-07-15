package org.codewith3h.finmateapplication.controller;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.codewith3h.finmateapplication.dto.request.BudgetPredictionRequest;
import org.codewith3h.finmateapplication.dto.response.*;
import org.codewith3h.finmateapplication.entity.Category;
import org.codewith3h.finmateapplication.entity.Goal;
import org.codewith3h.finmateapplication.entity.Transaction;
import org.codewith3h.finmateapplication.entity.UserCategory;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.service.*;
import org.codewith3h.finmateapplication.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/AI")
@RequiredArgsConstructor
@Slf4j
public class AIController {
    private final TransactionService transactionService;
    private final GoalService goalService;
    private final CategoryService categoryService;
    private final UserCategoryService userCategoryService;
    private final AIService aiService;

    @GetMapping("/data/retraining-data")
    public ResponseEntity<ApiResponse<RetrainingDataResponse>> getRetrainingData(){
        log.info("Fetching data for retraining AI");

        List<TransactionResponse> transactions =  transactionService.getAllTransactions();
        List<GoalResponse> goals = goalService.getAllGoals();
        List<CategoryResponse> defaultCategories =  categoryService.getAllCategories();
        List<CategoryResponse> userCategories = userCategoryService.getAllUserCategories();

        RetrainingDataResponse retrainingDataResponse = RetrainingDataResponse.builder()
                .transactions(transactions)
                .goals(goals)
                .defaultCategories(defaultCategories)
                .userCategories(userCategories)
                .build();

        ApiResponse<RetrainingDataResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Retraining data fetched successfully.");
        apiResponse.setResult(retrainingDataResponse);
        return ResponseEntity.ok(apiResponse);
    };

    @PostMapping("/predict-budget")
    public ResponseEntity<ApiResponse<BudgetPredictionResponse>> predictBudget(
            @RequestBody BudgetPredictionRequest budgetPredictionRequest,
            HttpServletRequest request) throws MessagingException {
        log.info("Predicting budget for user {}", budgetPredictionRequest.getUserId());
        String tokenHeader = request.getHeader("Authorization");
        if(!tokenHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization token");
        }

        String jwtToken = tokenHeader.substring(7);
        BudgetPredictionResponse budgetPredictionResponse =
                aiService.predictBudgets(budgetPredictionRequest.getUserId(), jwtToken);

        ApiResponse<BudgetPredictionResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Budgets predicted successfully.");
        apiResponse.setResult(budgetPredictionResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/retrain-model")
    public ResponseEntity<ApiResponse<RetrainResponse>>  retrainModel(HttpServletRequest request){
        log.info("Retraining model!");
        String tokenHeader = request.getHeader("Authorization");
        if(!tokenHeader.startsWith("Bearer ")){
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String jwtToken = tokenHeader.substring(7);
        RetrainResponse response = aiService.retrainModel(jwtToken);
        ApiResponse<RetrainResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Retraining model fetched successfully.");
        apiResponse.setResult(response);
        return ResponseEntity.ok(apiResponse);
    }
}
