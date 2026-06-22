package com.branch.inventory.backend.controller;

import com.branch.inventory.backend.dto.response.ApiResponse;
import com.branch.inventory.backend.dto.response.AuditLogResponse;
import com.branch.inventory.backend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin/audit-log")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getLogs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 10, sort = "performedAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {

        // Convert date → datetime boundaries (start of day / end of day)
        LocalDateTime fromDt = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDt = to != null ? to.atTime(23, 59, 59) : null;

        Page<AuditLogResponse> page = auditLogRepository
                .findFiltered(search, action, entityType, fromDt, toDt, pageable)
                .map(log -> AuditLogResponse.builder()
                        .id(log.getId())
                        .action(log.getAction())
                        .entityType(log.getEntityType())
                        .entityId(log.getEntityId())
                        .performedBy(log.getPerformedBy())
                        .details(log.getDetails())
                        .timestamp(log.getPerformedAt())
                        .build());

        return ResponseEntity.ok(ApiResponse.success(page));
    }
}
