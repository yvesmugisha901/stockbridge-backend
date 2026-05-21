package com.branch.inventory.backend.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateItemRequest {

    private String name;

    private String description;

    private String category;

    private String unitOfMeasure; // renamed from "unit" to match service's getUnitOfMeasure()

    private BigDecimal unitPrice;
}