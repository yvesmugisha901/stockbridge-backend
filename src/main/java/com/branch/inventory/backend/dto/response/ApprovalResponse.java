package com.branch.inventory.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApprovalResponse {

    private Long id;
    private Long transferRequestId;
    private int approvalTier;
    private Long approverId;
    private String approverName;
    private boolean approved;
    private String comment;
    private LocalDateTime decidedAt;
}