package org.codewith3h.finmateapplication.controller;

import jakarta.servlet.http.HttpSession;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.GoalProgressResponse;
import org.codewith3h.finmateapplication.service.GoalProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/goal_tracking")
public class GoalTrackingController {
    @Autowired
    GoalProgressService goalProgressService;

    @GetMapping("/{goalId}")
    public ResponseEntity<ApiResponse<GoalProgressResponse>> goalTracking(@PathVariable int goalId) {
        GoalProgressResponse goalProgressResponse = goalProgressService.getGoalProgressesByGoalId(goalId);
        ApiResponse<GoalProgressResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(goalProgressResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<GoalProgressResponse>>> getGoalProgresses(
            @RequestHeader(name = "userId") Integer userId,
            @RequestParam(name = "filter", required = false) String filter) {
        List<GoalProgressResponse> goalProgressResponseList = goalProgressService.getAllGoalProgressesUniqueByDate(userId, filter);
        ApiResponse<List<GoalProgressResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(goalProgressResponseList);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/list_progress/{goal_id}")
    public ResponseEntity<ApiResponse<List<GoalProgressResponse>>> getGoalProgress(@PathVariable(name = "goal_id") Integer goal_id) {
        List<GoalProgressResponse> list = goalProgressService.getListGoalProgressByGoalId(goal_id);
        ApiResponse<List<GoalProgressResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("List of goals progresses");
        apiResponse.setResult(list);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/store-goal-id")
    public void storeGoalId(@RequestBody String goalId, HttpSession session) {
        session.setAttribute("goalId", goalId);
    }

    @GetMapping("/get-goal-id-from-session")
    public String getGoalFromSession(HttpSession session) {
        return (String) session.getAttribute("goalId");
    }

}
