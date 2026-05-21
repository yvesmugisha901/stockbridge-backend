package com.branch.inventory.backend.controller;

import com.branch.inventory.backend.dto.response.ApiResponse;
import com.branch.inventory.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * ReportController – pre-built reporting and CSV export.
 *
 * FR-25: Stock Level Report
 * FR-26: Transfer History Report
 * FR-27: Low Stock Report (HO_ADMIN)
 * FR-28: CSV Export
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * GET /api/v1/reports/stock-levels
     * Stock level report filtered by branch, category, and item.
     * Accessible by MANAGER, HO_ADMIN, ACCOUNTANT, ADMIN.
     * FR-25
     */
    @GetMapping("/stock-levels")
    @PreAuthorize("hasAnyRole('MANAGER', 'HO_ADMIN', 'ACCOUNTANT', 'ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getStockLevelReport(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long itemId) {
        Object report = reportService.getStockLevelReport(branchId, category, itemId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    /**
     * GET /api/v1/reports/stock-levels/export
     * Exports the stock level report as a CSV file.
     * FR-28
     */
    @GetMapping("/stock-levels/export")
    @PreAuthorize("hasAnyRole('MANAGER', 'HO_ADMIN', 'ACCOUNTANT', 'ADMIN')")
    public ResponseEntity<byte[]> exportStockLevelReport(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long itemId) {
        byte[] csv = reportService.exportStockLevelReportCsv(branchId, category, itemId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"stock-levels-report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    /**
     * GET /api/v1/reports/transfer-history
     * Transfer history report filtered by branch, status, item, and date range.
     * FR-26, FR-30, FR-31, FR-32
     */
    @GetMapping("/transfer-history")
    @PreAuthorize("hasAnyRole('MANAGER', 'HO_ADMIN', 'ACCOUNTANT', 'ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getTransferHistoryReport(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        Object report = reportService.getTransferHistoryReport(
                branchId, status, itemId, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    /**
     * GET /api/v1/reports/transfer-history/export
     * Exports the transfer history report as a CSV file.
     * FR-28
     */
    @GetMapping("/transfer-history/export")
    @PreAuthorize("hasAnyRole('MANAGER', 'HO_ADMIN', 'ACCOUNTANT', 'ADMIN')")
    public ResponseEntity<byte[]> exportTransferHistoryReport(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        byte[] csv = reportService.exportTransferHistoryReportCsv(
                branchId, status, itemId, fromDate, toDate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"transfer-history-report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    /**
     * GET /api/v1/reports/low-stock
     * Consolidated report of all items below the minimum threshold across all
     * branches.
     * HO_ADMIN only.
     * FR-27
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('HO_ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getLowStockReport(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String category) {
        Object report = reportService.getLowStockReport(branchId, category);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    /**
     * GET /api/v1/reports/low-stock/export
     * Exports the low stock report as a CSV file.
     * FR-28
     */
    @GetMapping("/low-stock/export")
    @PreAuthorize("hasRole('HO_ADMIN')")
    public ResponseEntity<byte[]> exportLowStockReport(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String category) {
        byte[] csv = reportService.exportLowStockReportCsv(branchId, category);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"low-stock-report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}