package org.codewith3h.finmateapplication.controller;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.BudgetPredictionRequest;
import org.codewith3h.finmateapplication.dto.response.*;
import org.codewith3h.finmateapplication.service.*;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/AI")
@RequiredArgsConstructor
@Validated
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

    @GetMapping("/training-models")
    public ResponseEntity<ApiResponse<Page<RetrainResponse>>>  getTrainingModels(
            @RequestParam(defaultValue = "0", name = "page") @Min(0) int page,
            @RequestParam(defaultValue = "5", name = "size") @Min(1) int size,
            @RequestParam(defaultValue = "trainingTimestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection){
        log.info("page: {}", page);
        log.info("Fetching training histories");
        Page<RetrainResponse> retrainResponsePage = aiService.getModelTrainings(page, size, sortBy, sortDirection);
        ApiResponse<Page<RetrainResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Retraining models fetched successfully.");
        apiResponse.setResult(retrainResponsePage);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/stats-model")
    public ResponseEntity<ApiResponse<AiStatsResponse>> getAiStats(){
        log.info("Fetching statistic model");
        AiStatsResponse aiStatsResponse = aiService.getStatsModel();
        log.info("Statistic: {}", aiStatsResponse.getTotalCategories());
        ApiResponse<AiStatsResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Stats model fetched successfully.");
        apiResponse.setResult(aiStatsResponse);
        return ResponseEntity.ok(apiResponse);
    }
}
