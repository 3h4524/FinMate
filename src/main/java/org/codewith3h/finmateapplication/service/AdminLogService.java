package org.codewith3h.finmateapplication.service;

import lombok.RequiredArgsConstructor;
import org.codewith3h.finmateapplication.dto.response.AdminLogResponse;
import org.codewith3h.finmateapplication.entity.AdminLog;
import org.codewith3h.finmateapplication.repository.AdminLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminLogService {

    private final AdminLogRepository adminLogRepository;

    public Page<AdminLogResponse> getAdminLogsByFilter(LocalDate startDate, LocalDate endDate, String entityType, Integer adminId, Pageable pageable) {
        Instant start = startDate != null ? startDate.atStartOfDay(ZoneOffset.UTC).toInstant() : null;
        Instant end = endDate != null ? endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant() : null;

        if (startDate != null && endDate != null && entityType != null && adminId != null) {
            return adminLogRepository.findByCreatedAtBetweenAndEntityTypeAndAdminId(start, end, entityType, adminId, pageable)
                    .map(this::convertToResponse);
        } else if (startDate != null && endDate != null) {
            return adminLogRepository.findByCreatedAtBetween(start, end, pageable)
                    .map(this::convertToResponse);
        } else if (entityType != null) {
            return adminLogRepository.findByEntityType(entityType, pageable)
                    .map(this::convertToResponse);
        } else if (adminId != null) {
            return adminLogRepository.findByAdminId(adminId, pageable)
                    .map(this::convertToResponse);
        }
        return adminLogRepository.findAll(pageable)
                .map(this::convertToResponse);
    }

    public List<AdminLogResponse> getAllLogsForExport() {
        return adminLogRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private AdminLogResponse convertToResponse(AdminLog log) {
        return AdminLogResponse.builder()
                .id(log.getId())
                .adminId(log.getAdmin().getId())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .details(log.getDetails())
                .createdAt(log.getCreatedAt())
                .build();
    }
}