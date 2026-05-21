package com.branch.inventory.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockAlertResponse {

    private Long branchId;
    private String branchName;
    private Long itemId;
    private String itemName;
    private String itemCode;
    private int quantityOnHand;
    private int minimumThreshold;
    private int deficit;
}