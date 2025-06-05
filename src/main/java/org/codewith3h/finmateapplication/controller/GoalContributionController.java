package org.codewith3h.finmateapplication.controller;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.codewith3h.finmateapplication.dto.request.CreateGoalContributionRequest;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.GoalContributionResponse;
import org.codewith3h.finmateapplication.service.GoalContributionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contributions")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GoalContributionController {

    GoalContributionService goalContributionService;

    @GetMapping("/{goalId}")
    public ResponseEntity<ApiResponse<List<GoalContributionResponse>>> getContributions(@PathVariable int goalId) {
        List<GoalContributionResponse> contributions = goalContributionService.getContributionsByGoalId(goalId);
        ApiResponse<List<GoalContributionResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("List of contributions for goal " + goalId);
        apiResponse.setResult(contributions);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{goalId}")
    public ResponseEntity<ApiResponse<GoalContributionResponse>> createGoalContribution(@RequestBody CreateGoalContributionRequest request, @PathVariable int goalId) {
        request.setGoalId(goalId);
        GoalContributionResponse goalContributionResponse = goalContributionService.createGoalContribution(request);
        ApiResponse<GoalContributionResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Goal created successfully.");
        apiResponse.setResult(goalContributionResponse);
        return ResponseEntity.ok(apiResponse);
    }

}
