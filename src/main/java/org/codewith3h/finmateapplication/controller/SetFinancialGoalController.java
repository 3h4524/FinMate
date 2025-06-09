package org.codewith3h.finmateapplication.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.codewith3h.finmateapplication.dto.request.CreateGoalRequest;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.GoalResponse;
import org.codewith3h.finmateapplication.service.GoalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/goal")
public class SetFinancialGoalController {
    GoalService goalService;

    @PostMapping
    public ResponseEntity<ApiResponse<GoalResponse>> createGoal(@RequestBody @Valid CreateGoalRequest request) {
        GoalResponse goal = goalService.createFinancialGoal(request);
        ApiResponse<GoalResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Goal created successfully.");
        apiResponse.setResult(goal);
        return ResponseEntity.ok(apiResponse);
    }
}
