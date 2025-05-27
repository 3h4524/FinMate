package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory,Long> {
}
