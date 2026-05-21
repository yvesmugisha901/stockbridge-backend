package com.branch.inventory.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferCostResponse {

    private Long transferId;
    private String sourceBranchName;
    private String destinationBranchName;
    private String itemName;
    private int quantity;
    private BigDecimal transferValue;
    private String status;
    private BigDecimal costAmount;
    private String currency;
    private String costType;
    private String costNotes;
}