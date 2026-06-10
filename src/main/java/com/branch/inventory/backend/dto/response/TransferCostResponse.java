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
public class TransferCostResponse {

    private Long transferId;
    private Long sourceBranchId; // ← added: needed for branch filter
    private String sourceBranchName;
    private Long destinationBranchId; // ← added: needed for branch filter
    private String destinationBranchName;
    private String itemName;
    private int quantity;
    private BigDecimal transferValue;
    private String status;
    private BigDecimal costAmount;
    private String currency;
    private String costType;
    private String costNotes;
    private LocalDateTime requestedAt;
}
