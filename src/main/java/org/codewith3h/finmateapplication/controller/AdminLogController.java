package org.codewith3h.finmateapplication.controller;

import lombok.RequiredArgsConstructor;
import org.codewith3h.finmateapplication.dto.response.AdminLogResponse;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.service.AdminLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminLogController {

    private final AdminLogService adminLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminLogResponse>>> getAdminLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Integer adminId) {

        Pageable pageable = PageRequest.of(page, size);
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;
        Page<AdminLogResponse> logs = adminLogService.getAdminLogsByFilter(start, end, entityType, adminId, pageable);
        ApiResponse<Page<AdminLogResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(logs);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAdminLogs() {
        List<AdminLogResponse> logs = adminLogService.getAllLogsForExport();
        StringBuilder csvContent = new StringBuilder("LogID,AdminID,Action,EntityType,EntityID,Details,CreatedAt\n");
        for (AdminLogResponse log : logs) {
            csvContent.append(String.format("%d,%d,%s,%s,%d,%s,%s\n",
                    log.getId(), log.getAdminId(), log.getAction(), log.getEntityType(),
                    log.getEntityId() != null ? log.getEntityId() : 0,
                    log.getDetails() != null ? "\"" + log.getDetails().replace("\"", "\"\"") + "\"" : "",
                    log.getCreatedAt()));
        }
        byte[] csvBytes = csvContent.toString().getBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "admin_logs.csv");
        headers.setContentLength(csvBytes.length);
        return new ResponseEntity<>(csvBytes, headers, org.springframework.http.HttpStatus.OK);
    }
}