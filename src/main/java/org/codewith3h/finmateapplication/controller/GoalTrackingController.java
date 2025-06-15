package org.codewith3h.finmateapplication.controller;

import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.GoalProgressResponse;
import org.codewith3h.finmateapplication.service.GoalProgressService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/goal_tracking")
public class GoalTrackingController {
    GoalProgressService goalProgressService;

    @GetMapping("/{goalId}")
    public ResponseEntity<ApiResponse<GoalProgressResponse>> goalTracking(@PathVariable int goalId) {
        GoalProgressResponse goalProgressResponse = goalProgressService.getGoalProgressesByGoalId(goalId);
        ApiResponse<GoalProgressResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(goalProgressResponse);
        return ResponseEntity.ok(apiResponse);
    }

    // get all progress with status != cancel
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Page<GoalProgressResponse>>> getGoalProgresses(
            @RequestHeader(name = "userId") Integer userId,
            @RequestParam(name = "status", required = false, defaultValue = "CANCELLED") String status,
            @RequestParam(name = "page", defaultValue = "0", required = false) @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "100", required = false) @Min(1) int size) {
        System.out.println("getGoalProgresses");

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        System.err.println("user: " + authentication.getName());
        System.err.println("role: " + authentication.getAuthorities());


        Page<GoalProgressResponse> goalProgressResponseList = goalProgressService.getAllGoalProgressesUniqueByDate(userId, status, page, size);
        ApiResponse<Page<GoalProgressResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(goalProgressResponseList);
        apiResponse.setCode(1000);
        System.out.println("return getGoalProgresses");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/list_progress/{goal_id}")
    public ResponseEntity<ApiResponse<Page<GoalProgressResponse>>> getGoalProgress(
            @PathVariable(name = "goal_id") Integer goal_id,
            @RequestParam(name = "page", defaultValue = "0", required = false) @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "100", required = false) @Min(1) int size) {
        Page<GoalProgressResponse> list = goalProgressService.getListGoalProgressByGoalId(goal_id, page, size);
        ApiResponse<Page<GoalProgressResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("List of goals progresses");
        apiResponse.setResult(list);
        return ResponseEntity.ok(apiResponse);
    }

}
