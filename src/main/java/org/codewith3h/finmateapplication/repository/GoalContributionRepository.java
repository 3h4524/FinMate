package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.GoalContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoalContributionRepository extends JpaRepository<GoalContribution, Integer> {
}
