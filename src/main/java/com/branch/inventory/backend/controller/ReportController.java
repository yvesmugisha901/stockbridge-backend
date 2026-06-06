package com.branch.inventory.backend.controller;

import com.branch.inventory.backend.dto.response.ApiResponse;
import com.branch.inventory.backend.dto.response.StockLevelResponse;
import com.branch.inventory.backend.dto.response.TransferResponse;
import com.branch.inventory.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

        private final ReportService reportService;

        @GetMapping("/stock-levels")
        @PreAuthorize("hasAnyRole('MANAGER', 'HO_ADMIN', 'ACCOUNTANT', 'ADMIN')")
        public ResponseEntity<ApiResponse<List<StockLevelResponse>>> getStockLevelReport(
                        @RequestParam(required = false) Long branchId,
                        @RequestParam(required = false) String category,
                        @RequestParam(required = false) Long itemId) {
                return ResponseEntity.ok(ApiResponse.success(
                                reportService.getStockLevelReport(branchId, category, itemId)));
        }

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

        @GetMapping("/transfer-history")
        @PreAuthorize("hasAnyRole('MANAGER', 'HO_ADMIN', 'ACCOUNTANT', 'ADMIN')")
        public ResponseEntity<ApiResponse<List<TransferResponse>>> getTransferHistoryReport(
                        @RequestParam(required = false) Long branchId,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) Long itemId,
                        @RequestParam(required = false) String fromDate,
                        @RequestParam(required = false) String toDate) {
                return ResponseEntity.ok(ApiResponse.success(
                                reportService.getTransferHistoryReport(branchId, status, itemId, fromDate, toDate)));
        }

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

        @GetMapping("/low-stock")
        @PreAuthorize("hasRole('HO_ADMIN')")
        public ResponseEntity<ApiResponse<List<StockLevelResponse>>> getLowStockReport(
                        @RequestParam(required = false) Long branchId,
                        @RequestParam(required = false) String category) {
                return ResponseEntity.ok(ApiResponse.success(
                                reportService.getLowStockReport(branchId, category)));
        }

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