package com.branch.inventory.backend.controller;

import com.branch.inventory.backend.dto.request.StockAdjustmentRequest;
import com.branch.inventory.backend.dto.response.ApiResponse;
import com.branch.inventory.backend.dto.response.StockLevelResponse;
import com.branch.inventory.backend.dto.response.LowStockAlertResponse;
import com.branch.inventory.backend.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * StockController – per-branch stock level tracking and adjustments.
 * FR-10: Stock Level Tracking
 * FR-11: Manual Stock Adjustment
 * FR-12: Low Stock Alerts
 * FR-13: Stock Visibility (role-filtered)
 */
@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /**
     * GET /api/v1/stock
     * Returns stock levels for all branches (HO_ADMIN/ADMIN) or
     * own branch only (STAFF/MANAGER).
     * FR-10, FR-13
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<StockLevelResponse>>> getStock(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) String category,
            @AuthenticationPrincipal UserDetails currentUser,
            Pageable pageable) {
        Page<StockLevelResponse> stock = stockService.getStock(
                currentUser.getUsername(), branchId, itemId, category, pageable);
        return ResponseEntity.ok(ApiResponse.success(stock));
    }

    /**
     * GET /api/v1/stock/branch/{branchId}
     * Returns stock levels for a specific branch.
     * HO_ADMIN and ADMIN can access any branch; others are restricted to own
     * branch.
     * FR-13
     */
    @GetMapping("/branch/{branchId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<StockLevelResponse>>> getStockByBranch(
            @PathVariable Long branchId,
            @AuthenticationPrincipal UserDetails currentUser) {
        List<StockLevelResponse> stock = stockService.getStockByBranch(
                currentUser.getUsername(), branchId);
        return ResponseEntity.ok(ApiResponse.success(stock));
    }

    /**
     * GET /api/v1/stock/low-stock-alerts
     * Returns all items below the minimum threshold.
     * Visible on relevant role dashboards.
     * FR-12
     */
    @GetMapping("/low-stock-alerts")
    @PreAuthorize("hasAnyRole('HO_ADMIN', 'ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<LowStockAlertResponse>>> getLowStockAlerts(
            @RequestParam(required = false) Long branchId) {
        List<LowStockAlertResponse> alerts = stockService.getLowStockAlerts(branchId);
        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    /**
     * POST /api/v1/stock/adjust
     * HO_ADMIN records a manual stock adjustment (addition/removal)
     * with a reason and timestamp.
     * FR-11
     */
    @PostMapping("/adjust")
    @PreAuthorize("hasRole('HO_ADMIN')")
    public ResponseEntity<ApiResponse<StockLevelResponse>> adjustStock(
            @Valid @RequestBody StockAdjustmentRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        StockLevelResponse updated = stockService.adjustStock(
                request, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * GET /api/v1/stock/{branchId}/{itemId}
     * Returns stock details for a specific item at a specific branch.
     */
    @GetMapping("/{branchId}/{itemId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<StockLevelResponse>> getStockDetail(
            @PathVariable Long branchId,
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails currentUser) {
        StockLevelResponse stock = stockService.getStockDetail(
                currentUser.getUsername(), branchId, itemId);
        return ResponseEntity.ok(ApiResponse.success(stock));
    }
}