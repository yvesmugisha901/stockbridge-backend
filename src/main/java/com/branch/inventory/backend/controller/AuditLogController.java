package com.branch.inventory.backend.controller;

import com.branch.inventory.backend.dto.response.ApiResponse;
import com.branch.inventory.backend.dto.response.AuditLogResponse;
import com.branch.inventory.backend.model.AuditLog;
import com.branch.inventory.backend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/audit-log")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getLogs(
            @PageableDefault(size = 100, sort = "performedAt") Pageable pageable) {

        Page<AuditLogResponse> page = auditLogRepository.findAll(pageable)
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