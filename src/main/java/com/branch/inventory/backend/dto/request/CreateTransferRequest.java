package com.branch.inventory.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateTransferRequest {

    @NotNull(message = "Source branch is required")
    private Long sourceBranchId;

    @NotNull(message = "Destination branch is required")
    private Long destinationBranchId;

    @NotNull(message = "Item is required")
    private Long itemId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    private String justification; // renamed from "notes" to match service's getJustification()
}