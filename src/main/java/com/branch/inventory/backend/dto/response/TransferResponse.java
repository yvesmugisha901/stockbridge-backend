package com.branch.inventory.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {

    private Long id;
    private Long sourceBranchId;
    private String sourceBranchName;
    private Long destinationBranchId;
    private String destinationBranchName;
    private Long itemId;
    private String itemName;
    private String itemCode;
    private Integer quantity;
    private BigDecimal totalValue;
    private String justification;
    private String status;
    private boolean requiresHoApproval;
    private String managerComments;
    private String hoComments;
    private String requestedByEmail;
    private LocalDateTime requestedAt;
    private LocalDateTime dispatchedAt;
    private LocalDateTime receivedAt;
}