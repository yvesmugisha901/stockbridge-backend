package com.branch.inventory.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateItemRequest {

    @NotBlank(message = "Item name is required")
    private String name;

    @NotBlank(message = "Item code is required")
    private String code; // renamed from "sku" to match service's getCode()

    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    private String unitOfMeasure; // renamed from "unit" to match service's getUnitOfMeasure()

    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    private BigDecimal unitPrice;
}