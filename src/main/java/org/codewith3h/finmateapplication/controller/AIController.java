package org.codewith3h.finmateapplication.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.RetrainingDataResponse;
import org.codewith3h.finmateapplication.entity.Category;
import org.codewith3h.finmateapplication.entity.Goal;
import org.codewith3h.finmateapplication.entity.Transaction;
import org.codewith3h.finmateapplication.entity.UserCategory;
import org.codewith3h.finmateapplication.service.CategoryService;
import org.codewith3h.finmateapplication.service.GoalService;
import org.codewith3h.finmateapplication.service.TransactionService;
import org.codewith3h.finmateapplication.service.UserCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/data/retraining-data")
@RequiredArgsConstructor
@Slf4j
public class AIController {
    private final TransactionService transactionService;
    private final GoalService goalService;
    private final CategoryService categoryService;
    private final UserCategoryService userCategoryService;

    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RetrainingDataResponse>> getRetrainingData(){
        log.info("Fetching data for retraining AI");

        List<Transaction> transactions =  transactionService.getAllTransactions();
        List<Goal> goals = goalService.getAllGoals();
        List<Category> defaultCategories =  categoryService.getAllCategories();
        List<UserCategory> userCategories = userCategoryService.getAllUserCategories();

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

}
