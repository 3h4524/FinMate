package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.GoalContribution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalContributionRepository extends JpaRepository<GoalContribution, Long> {
    Page<GoalContribution> findGoalContributionsByGoal_Id(Integer goalId, Pageable pageable);
}
