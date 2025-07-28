package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.ModelTrainingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelTrainingRepository extends JpaRepository<ModelTrainingHistory, Integer> {
}
