package com.branch.inventory.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockAdjustmentRequest {

    @NotNull(message = "Item ID is required")
    private Long itemId;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    @NotNull(message = "Quantity is required")
    private Integer adjustmentQuantity; // renamed from "quantityChange" to match service's getAdjustmentQuantity()

    @NotBlank(message = "Reason is required")
    private String reason;
}