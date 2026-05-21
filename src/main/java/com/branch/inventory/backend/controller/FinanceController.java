package com.branch.inventory.backend.controller;

import com.branch.inventory.backend.dto.request.TransferCostRequest;
import com.branch.inventory.backend.dto.response.ApiResponse;
import com.branch.inventory.backend.dto.response.TransferCostResponse;
import com.branch.inventory.backend.dto.response.FinanceSummaryResponse;
import com.branch.inventory.backend.service.FinanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * FinanceController – Accountant / Finance module.
 *
 * FR-22: View Approved Transfers
 * FR-23: Record Transfer Cost
 * FR-24: Finance Summary
 */
@RestController
@RequestMapping("/api/v1/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    /**
     * GET /api/v1/finance/transfers
     * ACCOUNTANT views transfers with status: HO_APPROVED, IN_TRANSIT, RECEIVED,
     * COMPLETED.
     * Supports filtering by branch and date range.
     * FR-22, FR-30, FR-32
     */
    @GetMapping("/transfers")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<Page<TransferCostResponse>>> getApprovedTransfers(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        Page<TransferCostResponse> transfers = financeService.getApprovedTransfers(
                branchId, fromDate, toDate, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(transfers));
    }

    /**
     * GET /api/v1/finance/transfers/{transferId}
     * Returns the cost record for a specific transfer.
     * FR-22
     */
    @GetMapping("/transfers/{transferId}")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<TransferCostResponse>> getTransferCostDetails(
            @PathVariable Long transferId) {
        TransferCostResponse cost = financeService.getTransferCostDetails(transferId);
        return ResponseEntity.ok(ApiResponse.success(cost));
    }

    /**
     * POST /api/v1/finance/transfers/{transferId}/cost
     * ACCOUNTANT attaches a cost record to a completed transfer.
     * Fields: amount, currency, cost type, notes.
     * FR-23
     */
    @PostMapping("/transfers/{transferId}/cost")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<TransferCostResponse>> recordTransferCost(
            @PathVariable Long transferId,
            @Valid @RequestBody TransferCostRequest request) {
        TransferCostResponse cost = financeService.recordTransferCost(transferId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(cost));
    }

    /**
     * PUT /api/v1/finance/transfers/{transferId}/cost
     * ACCOUNTANT updates an existing cost record.
     * FR-23
     */
    @PutMapping("/transfers/{transferId}/cost")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<TransferCostResponse>> updateTransferCost(
            @PathVariable Long transferId,
            @Valid @RequestBody TransferCostRequest request) {
        TransferCostResponse cost = financeService.updateTransferCost(transferId, request);
        return ResponseEntity.ok(ApiResponse.success(cost));
    }

    /**
     * GET /api/v1/finance/summary
     * Returns a summary of total transfer costs filterable by date range and
     * branch.
     * FR-24
     */
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<FinanceSummaryResponse>> getFinanceSummary(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        FinanceSummaryResponse summary = financeService.getFinanceSummary(
                branchId, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}