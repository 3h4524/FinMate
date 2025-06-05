package org.codewith3h.finmateapplication.controller;

import org.codewith3h.finmateapplication.dto.request.CreateGoalRequest;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.GoalResponse;
import org.codewith3h.finmateapplication.service.GoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/goal")
public class SetFinancialGoalController {
    @Autowired
    private GoalService goalService;

    @PostMapping
    public ResponseEntity<ApiResponse<GoalResponse>> createGoal(@RequestBody CreateGoalRequest request, @RequestHeader(name = "userId") Integer userId) {
        request.setUserId(userId);
        GoalResponse goal = goalService.createFinancialGoal(request);
        ApiResponse<GoalResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Goal created successfully.");
        apiResponse.setResult(goal);
        return ResponseEntity.ok(apiResponse);
    }
}
