package com.branch.inventory.backend.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {

    private Long id;
    private String referenceNumber;

    private Long sourceBranchId;
    private String sourceBranchName;

    private Long destinationBranchId;
    private String destinationBranchName;

    private Long itemId;
    private String itemName;
    private String itemCode;

    private int quantity;
    private BigDecimal totalValue;
    private String justification;
    private String status;
    private boolean requiresHoApproval;

    // FIX: added requestedByName — was missing, causing "Requested By" to show null
    // in the dashboard table. The frontend getRequestedBy() helper tries
    // requestedByName first.
    private String requestedByName;
    private String requestedByEmail;

    private String managerComments;
    private String hoComments;

    private LocalDateTime requestedAt;
    private LocalDateTime dispatchedAt;
    private LocalDateTime receivedAt;
}