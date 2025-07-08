package org.codewith3h.finmateapplication.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.codewith3h.finmateapplication.dto.request.CreateGoalRequest;
import org.codewith3h.finmateapplication.dto.request.GoalUpdateRequest;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.GoalResponse;
import org.codewith3h.finmateapplication.service.GoalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/goal")
public class GoalController {
    GoalService goalService;

    @PostMapping
    public ResponseEntity<ApiResponse<GoalResponse>> createGoal(@RequestBody @Valid CreateGoalRequest request) {
        GoalResponse goal = goalService.createFinancialGoal(request);
        ApiResponse<GoalResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Congratulations you have successfully created a new goal!");
        apiResponse.setCode(1000);
        apiResponse.setResult(goal);
        return ResponseEntity.ok(apiResponse);
    }

    @PatchMapping("/cancel/{goalId}")
    public ResponseEntity<Void> cancelGoal(@PathVariable(name = "goalId") Integer goalId) {
        goalService.cancelFinancialGoal(goalId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{goalId}")
    public ResponseEntity<ApiResponse<GoalResponse>> updateGoal(@RequestBody @Valid GoalUpdateRequest request, @PathVariable Integer goalId) {
        GoalResponse response = goalService.updateGoal(request, goalId);
        ApiResponse<GoalResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Goal updated successfully.");
        apiResponse.setResult(response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{goalId}")
    public ResponseEntity<ApiResponse<GoalResponse>> getGoal(@PathVariable(name = "goalId") Integer goalId) {
        GoalResponse goal = goalService.getGoal(goalId);
        ApiResponse<GoalResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Goal found.");
        apiResponse.setResult(goal);
        return ResponseEntity.ok(apiResponse);
    }
}
