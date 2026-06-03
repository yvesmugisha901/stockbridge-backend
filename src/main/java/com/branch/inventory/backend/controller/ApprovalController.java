package com.branch.inventory.backend.controller;

import com.branch.inventory.backend.dto.request.ApprovalDecisionRequest;
import com.branch.inventory.backend.dto.response.ApiResponse;
import com.branch.inventory.backend.dto.response.TransferResponse;
import com.branch.inventory.backend.service.ApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * ApprovalController – handles the multi-tier approval workflow.
 *
 * Approval tiers (FR-15):
 * - Below thresholds → MANAGER only
 * - Above thresholds → MANAGER then HO_ADMIN
 *
 * FR-15: Approval Routing Logic
 * FR-16: First-Level Approval (MANAGER)
 * FR-17: Second-Level Approval (HO_ADMIN)
 * FR-20: Reject at any level
 * FR-21: Audit trail recorded in service layer
 */
@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
public class ApprovalController {

        private final ApprovalService approvalService;

        /**
         * GET /api/v1/approvals/pending/manager
         * Returns transfers pending first-level approval for the authenticated Manager.
         * Only shows transfers from the Manager's own branch.
         * FR-16
         */
        @GetMapping("/pending/manager")
        @PreAuthorize("hasRole('MANAGER')")
        public ResponseEntity<ApiResponse<Page<TransferResponse>>> getPendingForManager(
                        @AuthenticationPrincipal UserDetails currentUser,
                        Pageable pageable) {
                Page<TransferResponse> pending = approvalService.getPendingForManager(
                                currentUser.getUsername(), pageable);
                return ResponseEntity.ok(ApiResponse.success(pending));
        }

        /**
         * GET /api/v1/approvals/pending/head-office
         * Returns transfers pending second-level approval (MANAGER_APPROVED status).
         * FR-17
         */
        @GetMapping("/pending/head-office")
        @PreAuthorize("hasRole('HO_ADMIN')")
        public ResponseEntity<ApiResponse<Page<TransferResponse>>> getPendingForHeadOffice(
                        Pageable pageable) {
                Page<TransferResponse> pending = approvalService.getPendingForHeadOffice(pageable);
                return ResponseEntity.ok(ApiResponse.success(pending));
        }

        /**
         * POST /api/v1/approvals/{transferId}/manager-approve
         * Branch Manager approves a PENDING transfer request (first-level).
         * Mandatory comments required on rejection.
         * FR-16
         */
        @PostMapping("/{transferId}/manager-approve")
        @PreAuthorize("hasRole('MANAGER')")
        public ResponseEntity<ApiResponse<TransferResponse>> managerApprove(
                        @PathVariable Long transferId,
                        @Valid @RequestBody ApprovalDecisionRequest request,
                        @AuthenticationPrincipal UserDetails currentUser) {
                TransferResponse transfer = approvalService.managerDecision(
                                transferId, request, currentUser.getUsername());
                return ResponseEntity.ok(ApiResponse.success(transfer));
        }

        /**
         * POST /api/v1/approvals/{transferId}/manager-reject
         * Branch Manager rejects a PENDING transfer request.
         * Rejection reason (comment) is mandatory.
         * FR-16, FR-20
         */
        @PostMapping("/{transferId}/manager-reject")
        @PreAuthorize("hasRole('MANAGER')")
        public ResponseEntity<ApiResponse<TransferResponse>> managerReject(
                        @PathVariable Long transferId,
                        @Valid @RequestBody ApprovalDecisionRequest request,
                        @AuthenticationPrincipal UserDetails currentUser) {
                request.setApproved(false); // Ensure it's marked as rejection
                TransferResponse transfer = approvalService.managerDecision(
                                transferId, request, currentUser.getUsername());
                return ResponseEntity.ok(ApiResponse.success(transfer));
        }

        /**
         * POST /api/v1/approvals/{transferId}/ho-approve
         * HO_ADMIN gives final approval to a MANAGER_APPROVED transfer.
         * FR-17
         */
        @PostMapping("/{transferId}/ho-approve")
        @PreAuthorize("hasRole('HO_ADMIN')")
        public ResponseEntity<ApiResponse<TransferResponse>> headOfficeApprove(
                        @PathVariable Long transferId,
                        @Valid @RequestBody ApprovalDecisionRequest request,
                        @AuthenticationPrincipal UserDetails currentUser) {
                request.setApproved(true);
                TransferResponse transfer = approvalService.headOfficeDecision(
                                transferId, request, currentUser.getUsername());
                return ResponseEntity.ok(ApiResponse.success(transfer));
        }

        /**
         * POST /api/v1/approvals/{transferId}/ho-reject
         * HO_ADMIN rejects a MANAGER_APPROVED transfer. No stock movement occurs.
         * FR-17, FR-20
         */
        @PostMapping("/{transferId}/ho-reject")
        @PreAuthorize("hasRole('HO_ADMIN')")
        public ResponseEntity<ApiResponse<TransferResponse>> headOfficeReject(
                        @PathVariable Long transferId,
                        @Valid @RequestBody ApprovalDecisionRequest request,
                        @AuthenticationPrincipal UserDetails currentUser) {
                request.setApproved(false);
                TransferResponse transfer = approvalService.headOfficeDecision(
                                transferId, request, currentUser.getUsername());
                return ResponseEntity.ok(ApiResponse.success(transfer));
        }
        // ─── Add these two endpoints to your existing ApprovalController
        // ──────────────

        // GET /api/v1/approvals/pending/dispatch
        // Used by: DispatchApprovalsPage — source branch manager's queue
        @GetMapping("/pending/dispatch")
        @PreAuthorize("hasRole('MANAGER')")
        public ResponseEntity<ApiResponse<Page<TransferResponse>>> getPendingDispatch(
                        @AuthenticationPrincipal UserDetails userDetails,
                        Pageable pageable) {
                return ResponseEntity.ok(ApiResponse.success(
                                approvalService.getPendingDispatch(userDetails.getUsername(), pageable)));
        }

        // POST /api/v1/approvals/{transferId}/dispatch
        // Used by: DispatchApprovalsPage — marks HO_APPROVED transfer as IN_TRANSIT
        @PostMapping("/{transferId}/dispatch")
        @PreAuthorize("hasRole('MANAGER')")
        public ResponseEntity<ApiResponse<TransferResponse>> dispatch(
                        @PathVariable Long transferId,
                        @AuthenticationPrincipal UserDetails userDetails) {
                return ResponseEntity.ok(ApiResponse.success(
                                approvalService.dispatchTransfer(transferId, userDetails.getUsername())));
        }
}