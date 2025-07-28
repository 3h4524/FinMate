package org.codewith3h.finmateapplication.service;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.AdminLogCreateRequest;
import org.codewith3h.finmateapplication.dto.response.AdminLogResponse;
import org.codewith3h.finmateapplication.entity.AdminLog;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.AdminLogMapper;
import org.codewith3h.finmateapplication.repository.AdminLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Data
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminLogService {

    AdminLogRepository adminLogRepository;
    AdminLogMapper adminLogMapper;

    public void createAdminLog(AdminLogCreateRequest request) {

        log.info("Creating new admin log for adminId: {}, action: {}, entityType: {}",
                request.getAdminId(), request.getAction(), request.getEntityType());

        AdminLog adminLog = adminLogRepository.save(adminLogMapper.toAdminLog(request));

        log.info("Admin log created successfully with id: {}", adminLog.getId());
    }

    public Page<AdminLogResponse> getAdminLogsByFilter(
            LocalDate startDate, LocalDate endDate, String entityType,
            Integer adminId, String keyword, Pageable pageable) {
        log.info("Fetching admin logs with filters: startDate={}, endDate={}, entityType={}, adminId={}, keyword={}",
                startDate, endDate, entityType, adminId, keyword);

        Instant start = startDate != null ? startDate.atStartOfDay(ZoneOffset.UTC).toInstant() : null;
        Instant end = endDate != null ? endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant() : null;

        Page<AdminLog> logs;
        if (keyword != null && !keyword.isEmpty()) {
            logs = adminLogRepository.findByKeyword(keyword, pageable);
        } else if (start != null && end != null && entityType != null && adminId != null) {
            logs = adminLogRepository.findByCreatedAtBetweenAndEntityTypeAndAdminId(start, end, entityType, adminId, pageable);
        } else if (start != null && end != null && entityType != null) {
            logs = adminLogRepository.findByCreatedAtBetweenAndEntityType(start, end, entityType, pageable);
        } else if (start != null && end != null) {
            logs = adminLogRepository.findByCreatedAtBetween(start, end, pageable);
        } else if (entityType != null) {
            logs = adminLogRepository.findByEntityType(entityType, pageable);
        } else if (adminId != null) {
            logs = adminLogRepository.findByAdminId(adminId, pageable);
        } else {
            logs = adminLogRepository.findAll(pageable);
        }

        log.info("Retrieved {} admin logs", logs.getTotalElements());
        return logs.map(adminLogMapper::toAdminLogResponse);
    }

    public List<AdminLogResponse> getAllLogsForExport() {
        log.info("Fetching all admin logs for export");
        List<AdminLogResponse> logs = adminLogRepository.findAll()
                .stream()
                .map(adminLogMapper::toAdminLogResponse)
                .collect(Collectors.toList());
        log.info("Retrieved {} admin logs for export", logs.size());
        return logs;
    }

    public AdminLogResponse getLogById(Integer id) {
        log.info("Fetching admin log with id: {}", id);

        AdminLog adminLog = adminLogRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Admin log not found for id: {}", id);
                    return new AppException(ErrorCode.ADMIN_LOG_NOT_FOUND);
                });

        log.info("Admin log retrieved successfully for id: {}", id);
        return adminLogMapper.toAdminLogResponse(adminLog);
    }


    public Map<String, Object> getLogStats(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching admin log stats for startDate: {}, endDate: {}", startDate, endDate);

        Instant start = startDate != null ? startDate.atStartOfDay(ZoneOffset.UTC).toInstant() : null;
        Instant end = endDate != null ? endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant() : null;

        List<AdminLog> logs = (start != null && end != null)
                ? adminLogRepository.findByCreatedAtBetween(start, end, Pageable.unpaged()).getContent()
                : adminLogRepository.findAll();

        Map<String, Object> stats = Map.of(
                "totalLogs", logs.size(),
                "totalAdmins", logs.stream().map(AdminLog::getAdmin).map(admin -> admin.getId()).distinct().count(), // Số Admin duy nhất
                "actionStats", logs.stream().collect(Collectors.groupingBy(AdminLog::getAction, Collectors.counting())),
                "entityTypeStats", logs.stream().collect(Collectors.groupingBy(AdminLog::getEntityType, Collectors.counting()))
        );

        log.info("Retrieved stats with total logs: {}", logs.size());
        return stats;
    }

}
