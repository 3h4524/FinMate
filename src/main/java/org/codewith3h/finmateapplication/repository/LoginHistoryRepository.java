package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
}
