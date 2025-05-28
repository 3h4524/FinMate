package org.codewith3h.finmateapplication.service;

import org.codewith3h.finmateapplication.entity.Goal;
import org.codewith3h.finmateapplication.repository.GoalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoalService {
    @Autowired
    private GoalRepository goalRepository;

    public List<Goal> getAllGoals() {
        return goalRepository.findAll();
    }
}
