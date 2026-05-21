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
public class ItemResponse {

    private Long id;
    private String name;
    private String code; // renamed from "sku" to match service's .code()
    private String description;
    private String category;
    private String unitOfMeasure; // renamed from "unit" to match service's .unitOfMeasure()
    private BigDecimal unitPrice;
    private boolean active;
    private LocalDateTime createdAt;
}