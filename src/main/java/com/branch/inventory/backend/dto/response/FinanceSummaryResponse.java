package com.branch.inventory.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceSummaryResponse {

    private long totalTransfers;
    private BigDecimal totalCost;
    private Long branchId;
    private LocalDate fromDate;
    private LocalDate toDate;
}