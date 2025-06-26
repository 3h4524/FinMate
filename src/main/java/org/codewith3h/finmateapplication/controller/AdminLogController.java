package org.codewith3h.finmateapplication.controller;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.response.AdminLogResponse;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.service.AdminLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/logs")
@Data
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminLogController {

    AdminLogService adminLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminLogResponse>>> getAdminLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestParam(name = "entityType", required = false) String entityType,
            @RequestParam(name = "adminId", required = false) Integer adminId,
            @RequestParam(name = "keyword", required = false) String keyword) {

        log.info("Fetching admin logs with page: {}, size: {}, filters: startDate={}, endDate={}, entityType={}, adminId={}, keyword={}",
                page, size, startDate, endDate, entityType, adminId, keyword);

        Pageable pageable = PageRequest.of(page, size);
        Page<AdminLogResponse> logs = adminLogService.getAdminLogsByFilter(
                startDate != null ? LocalDate.parse(startDate) : null,
                endDate != null ? LocalDate.parse(endDate) : null,
                entityType, adminId, keyword, pageable);

        log.info("Retrieved {} admin logs", logs.getTotalElements());
        ApiResponse<Page<AdminLogResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(logs);
        apiResponse.setCode(1000);
        apiResponse.setMessage("Success");

        return ResponseEntity.ok(apiResponse);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminLogResponse>> getLogById(@PathVariable Integer id) {
        log.info("Fetching admin log with id: {}", id);
        AdminLogResponse adminLogResponse = adminLogService.getLogById(id);
        log.info("Admin log retrieved successfully for id: {}", id);
        ApiResponse<AdminLogResponse> apiResponse = new ApiResponse<>();
        apiResponse.setCode(1000);
        apiResponse.setMessage("Success");
        apiResponse.setResult(adminLogResponse);

        return ResponseEntity.ok(apiResponse);
    }


    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLogStats(
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate) {
        log.info("Fetching admin log stats with startDate: {}, endDate: {}", startDate, endDate);
        Map<String, Object> stats = adminLogService.getLogStats(
                startDate != null ? LocalDate.parse(startDate) : null,
                endDate != null ? LocalDate.parse(endDate) : null);
        log.info("Admin log stats retrieved successfully");
        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>();
        apiResponse.setCode(1000);
        apiResponse.setMessage("Success");
        apiResponse.setResult(stats);

        return ResponseEntity.ok(apiResponse);
    }


    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAdminLogs() {
        log.info("Exporting admin logs to CSV");
        List<AdminLogResponse> logs = adminLogService.getAllLogsForExport();

        StringBuilder csvContent = new StringBuilder("LogID,AdminID,Action,EntityType,EntityID,Details,CreatedAt\n");
        for (AdminLogResponse log : logs) {
            csvContent.append(String.format("%d,%d,%s,%s,%d,%s,%s\n",
                    log.getId(), log.getAdminId(),
                    escapeCsv(log.getAction()),
                    escapeCsv(log.getEntityType()),
                    log.getEntityId() != null ? log.getEntityId() : 0,
                    log.getDetails() != null ? "\"" + escapeCsv(log.getDetails()) + "\"" : "",
                    log.getCreatedAt()));
        }

        byte[] csvBytes = csvContent.toString().getBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "admin_logs.csv");
        headers.setContentLength(csvBytes.length);
        log.info("Exported {} admin logs to CSV", logs.size());

        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

}