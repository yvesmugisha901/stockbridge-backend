package com.branch.inventory.backend.controller;

import com.branch.inventory.backend.dto.request.CreateTransferRequest;
import com.branch.inventory.backend.dto.response.ApiResponse;
import com.branch.inventory.backend.dto.response.TransferResponse;
import com.branch.inventory.backend.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * TransferController – manages the full stock transfer lifecycle.
 *
 * Status lifecycle (FR-14 → FR-21):
 * PENDING → MANAGER_APPROVED → HO_APPROVED → IN_TRANSIT → RECEIVED → COMPLETED
 *
 * FR-14: Submit Transfer Request
 * FR-18: Mark In Transit
 * FR-19: Confirm Receipt
 * FR-20: Reject / Cancel
 * FR-21: Transfer Audit Trail (handled in service layer)
 */
@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    /**
     * POST /api/v1/transfers
     * STAFF or MANAGER submits a new transfer request.
     * FR-14, FR-15
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
    public ResponseEntity<ApiResponse<TransferResponse>> createTransfer(
            @Valid @RequestBody CreateTransferRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        TransferResponse transfer = transferService.createTransfer(
                request, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(transfer));
    }

    /**
     * GET /api/v1/transfers
     * Returns paginated transfer list filtered by branch, status, item, date range.
     * Visibility is role-filtered in the service layer.
     * FR-26, FR-30, FR-31, FR-32
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<TransferResponse>>> getTransfers(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @AuthenticationPrincipal UserDetails currentUser,
            Pageable pageable) {
        Page<TransferResponse> transfers = transferService.getTransfers(
                currentUser.getUsername(), branchId, status, itemId, fromDate, toDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(transfers));
    }

    /**
     * GET /api/v1/transfers/my
     * Returns paginated list of transfers submitted by the logged-in user.
     * MUST be declared before /{id} to prevent "my" being parsed as a Long.
     * FR-14, FR-21
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
    public ResponseEntity<ApiResponse<Page<TransferResponse>>> getMyTransfers(
            @AuthenticationPrincipal UserDetails currentUser,
            Pageable pageable) {
        Page<TransferResponse> transfers = transferService.getMyTransfers(
                currentUser.getUsername(), pageable);
        return ResponseEntity.ok(ApiResponse.success(transfers));
    }

    /**
     * GET /api/v1/transfers/{id}
     * Returns full details of a specific transfer including audit trail.
     * FR-21
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TransferResponse>> getTransferById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        TransferResponse transfer = transferService.getTransferById(id, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success(transfer));
    }

    /**
     * PATCH /api/v1/transfers/{id}/mark-in-transit
     * Source branch STAFF marks the transfer as In Transit after physical dispatch.
     * Requires status = HO_APPROVED or MANAGER_APPROVED.
     * FR-18
     */
    @PatchMapping("/{id}/mark-in-transit")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
    public ResponseEntity<ApiResponse<TransferResponse>> markInTransit(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        TransferResponse transfer = transferService.markInTransit(id, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success(transfer));
    }

    /**
     * PATCH /api/v1/transfers/{id}/confirm-receipt
     * Destination branch STAFF confirms receipt, triggering automatic stock
     * updates.
     * FR-19
     */
    @PatchMapping("/{id}/confirm-receipt")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
    public ResponseEntity<ApiResponse<TransferResponse>> confirmReceipt(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        TransferResponse transfer = transferService.confirmReceipt(id, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success(transfer));
    }

    /**
     * PATCH /api/v1/transfers/{id}/cancel
     * Requester cancels a PENDING transfer. No stock movement occurs.
     * FR-20
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
    public ResponseEntity<ApiResponse<TransferResponse>> cancelTransfer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        TransferResponse transfer = transferService.cancelTransfer(id, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success(transfer));
    }

    // ─── ADD THESE TWO ENDPOINTS to TransferController.java ──────────────────────
    // Place them alongside the existing @GetMapping methods

    /**
     * GET /api/v1/transfers/ready-to-dispatch
     * Returns approved transfers waiting to be shipped FROM the user's branch.
     * Used by source branch staff to click "Mark In Transit".
     */
    @GetMapping("/ready-to-dispatch")
    public ResponseEntity<ApiResponse<Page<TransferResponse>>> getReadyToDispatch(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                transferService.getReadyToDispatch(userDetails.getUsername(), pageable)));
    }

    /**
     * GET /api/v1/transfers/incoming
     * Returns IN_TRANSIT transfers heading TO the user's branch.
     * Used by destination branch staff to click "Confirm Receipt".
     */
    @GetMapping("/incoming")
    public ResponseEntity<ApiResponse<Page<TransferResponse>>> getIncomingTransfers(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                transferService.getIncomingTransfers(userDetails.getUsername(), pageable)));
    }
}
