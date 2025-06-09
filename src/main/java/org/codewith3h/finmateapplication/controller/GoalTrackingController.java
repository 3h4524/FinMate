package org.codewith3h.finmateapplication.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.GoalProgressResponse;
import org.codewith3h.finmateapplication.service.GoalProgressService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
            @RequestParam(name = "size", defaultValue = "10", required = false) @Min(1) int size) {
        System.out.println("getGoalProgresses");
        Page<GoalProgressResponse> goalProgressResponseList = goalProgressService.getAllGoalProgressesUniqueByDate(userId, status, page, size);
        ApiResponse<Page<GoalProgressResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(goalProgressResponseList);
        System.out.println("return getGoalProgresses");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/list_progress/{goal_id}")
    public ResponseEntity<ApiResponse<Page<GoalProgressResponse>>> getGoalProgress(
            @PathVariable(name = "goal_id") Integer goal_id,
            @RequestParam(name = "page", defaultValue = "0", required = false) @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) @Min(1) int size) {
        Page<GoalProgressResponse> list = goalProgressService.getListGoalProgressByGoalId(goal_id, page, size);
        ApiResponse<Page<GoalProgressResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("List of goals progresses");
        apiResponse.setResult(list);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/store-goal-id")
    public void storeGoalId(@RequestBody Map<String, String> body, HttpSession session) {
        String goalId = body.get("goalId");
        System.out.println("storeGoalId: " + goalId);
        session.setAttribute("goalId", goalId);
    }

    @GetMapping("/get-goal-id-from-session")
    public ResponseEntity<ApiResponse<String>> getGoalIdFromSession(HttpSession session) {
        String goalId = (String) session.getAttribute("goalId");
        System.out.println("getGoalIdFromSession: " + goalId);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Getting goal id from session");
        apiResponse.setResult(goalId);
        return ResponseEntity.ok(apiResponse);
    }

}
