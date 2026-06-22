package com.branch.inventory.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogResponse {
    private Long id;
    private String action;
    private String entityType;
    private Long entityId;
    private String performedBy;
    private String details;
    private LocalDateTime timestamp; // maps from model's performedAt
}