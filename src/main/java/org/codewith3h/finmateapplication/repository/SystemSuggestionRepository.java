package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.SystemSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemSuggestionRepository extends JpaRepository<SystemSuggestion, Long> {
}
