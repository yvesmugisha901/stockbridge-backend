package com.branch.inventory.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockLevelResponse {

    private Long id;
    private Long branchId;
    private String branchName;
    private Long itemId;
    private String itemName;
    private String itemCode;
    private String category;
    private Integer quantityOnHand;
    private Integer reservedQuantity;
    private Integer minimumThreshold;
    private boolean isLowStock;
    private LocalDateTime lastUpdated;
}