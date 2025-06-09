package org.codewith3h.finmateapplication.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.codewith3h.finmateapplication.dto.request.CreateGoalContributionRequest;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.GoalContributionResponse;
import org.codewith3h.finmateapplication.service.GoalContributionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contributions")
@Data
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GoalContributionController {

    GoalContributionService goalContributionService;

    @GetMapping("/{goalId}")
    public ResponseEntity<ApiResponse<Page<GoalContributionResponse>>> getContributions(
            @PathVariable int goalId,
            @RequestParam(name = "page", defaultValue = "0", required = false) @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) @Min(1) int size) {
        Page<GoalContributionResponse> contributions = goalContributionService.getContributionsByGoalId(goalId, PageRequest.of(page, size));
        ApiResponse<Page<GoalContributionResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("List of contributions for goal " + goalId);
        apiResponse.setResult(contributions);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{goalId}")
    public ResponseEntity<ApiResponse<GoalContributionResponse>> createGoalContribution(@RequestBody @Valid CreateGoalContributionRequest request) {
        GoalContributionResponse goalContributionResponse = goalContributionService.createGoalContribution(request);
        ApiResponse<GoalContributionResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Goal Contribution created successfully.");
        apiResponse.setResult(goalContributionResponse);
        return ResponseEntity.ok(apiResponse);
    }

}
