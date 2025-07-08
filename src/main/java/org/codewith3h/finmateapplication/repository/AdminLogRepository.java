package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.AdminLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface AdminLogRepository extends JpaRepository<AdminLog, Integer> {
    Page<AdminLog> findByCreatedAtBetweenAndEntityTypeAndAdminId(Instant startDate, Instant endDate, String entityType, Integer adminId, Pageable pageable);

    Page<AdminLog> findByCreatedAtBetween(Instant startDate, Instant endDate, Pageable pageable);

    Page<AdminLog> findByEntityType(String entityType, Pageable pageable);

    Page<AdminLog> findByAdminId(Integer adminId, Pageable pageable);

    Page<AdminLog> findByCreatedAtBetweenAndEntityType(Instant startDate, Instant endDate, String entityType, Pageable pageable);

    @Query("SELECT l FROM AdminLog l WHERE l.details LIKE %:keyword% OR l.action LIKE %:keyword% OR l.entityType LIKE %:keyword%")
    Page<AdminLog> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}