package org.codewith3h.finmateapplication.controller;

import org.codewith3h.finmateapplication.entity.Goal;
import org.codewith3h.finmateapplication.service.GoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/financial")
public class SetFinancialGoalController {
    @Autowired
    private GoalService goalService;
    @GetMapping()
    public List<Goal> getListGoals() {
        return goalService.getAllGoals();
    }
}
