package com.branch.inventory.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchSummaryResponse {

    private Long id;
    private String name;
    private String code;
    private String location;
    private String contactInfo;
    private boolean active;
    private int totalItems;
    private int lowStockCount;
}