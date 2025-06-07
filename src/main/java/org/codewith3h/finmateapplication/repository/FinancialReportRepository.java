package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.FinancialReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinancialReportRepository extends JpaRepository<FinancialReport,Long> {
}
