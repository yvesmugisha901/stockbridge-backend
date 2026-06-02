package com.branch.inventory.backend.service;

import com.branch.inventory.backend.dto.request.ApprovalDecisionRequest;
import com.branch.inventory.backend.dto.response.TransferResponse;
import com.branch.inventory.backend.model.TransferRequest;
import com.branch.inventory.backend.model.User;
import com.branch.inventory.backend.model.enums.TransferStatus;
import com.branch.inventory.backend.repository.TransferRequestRepository;
import com.branch.inventory.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final TransferRequestRepository transferRequestRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService; // ← added

    // ─── Manager queue ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<TransferResponse> getPendingForManager(String email, Pageable pageable) {
        User manager = getUser(email);
        if (manager.getBranch() == null) {
            throw new RuntimeException("Manager is not assigned to a branch");
        }
        return transferRequestRepository
                .findByStatusAndSourceBranch(TransferStatus.PENDING, manager.getBranch(), pageable)
                .map(this::mapToResponse);
    }

    // ─── HO queue ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<TransferResponse> getPendingForHeadOffice(Pageable pageable) {
        return transferRequestRepository
                .findByStatus(TransferStatus.MANAGER_APPROVED, pageable)
                .map(this::mapToResponse);
    }

    // ─── Manager approve / reject ─────────────────────────────────────────────

    @Transactional
    public TransferResponse managerDecision(Long transferId,
            ApprovalDecisionRequest request,
            String email) {

        // Eager fetch so requestedBy / item / branches are available for notification
        TransferRequest transfer = getTransferWithDetails(transferId);
        assertStatus(transfer, TransferStatus.PENDING);

        if (!request.isApproved() && isBlank(request.getComments())) {
            throw new RuntimeException("Rejection reason (comments) is mandatory");
        }

        User manager = getUser(email);

        if (request.isApproved()) {
            TransferStatus nextStatus = transfer.isRequiresHoApproval()
                    ? TransferStatus.MANAGER_APPROVED
                    : TransferStatus.HO_APPROVED;

            transfer.setStatus(nextStatus);
            transfer.setManagerApprovedBy(manager);
            transfer.setManagerApprovedAt(LocalDateTime.now());
            transfer.setManagerComments(request.getComments());
            transferRequestRepository.save(transfer);

            auditLogService.log("MANAGER_APPROVED", "TransferRequest", transferId, email,
                    "Manager approved. Next status: " + nextStatus);

            if (nextStatus == TransferStatus.MANAGER_APPROVED) {
                // Still needs HO approval — notify HO admins
                userRepository.findByRole(com.branch.inventory.backend.model.enums.Role.HO_ADMIN)
                        .forEach(admin -> notificationService.send(
                                admin.getId(),
                                "Transfer #" + transferId + " Awaiting HO Approval",
                                transfer.getQuantity() + "x " + transfer.getItem().getName()
                                        + " from " + transfer.getSourceBranch().getName()
                                        + " has been approved by the branch manager and needs your review.",
                                "TRANSFER_PENDING"));
            }

            // Notify the requester their transfer was approved
            notificationService.send(
                    transfer.getRequestedBy().getId(),
                    "Transfer #" + transferId + " Approved",
                    "Your request for " + transfer.getQuantity() + "x "
                            + transfer.getItem().getName() + " has been approved"
                            + (nextStatus == TransferStatus.MANAGER_APPROVED
                                    ? " by your branch manager and is awaiting Head Office review."
                                    : "."),
                    "TRANSFER_APPROVED");

        } else {
            transfer.setStatus(TransferStatus.REJECTED);
            transfer.setManagerApprovedBy(manager);
            transfer.setManagerApprovedAt(LocalDateTime.now());
            transfer.setManagerComments(request.getComments());
            transferRequestRepository.save(transfer);

            auditLogService.log("MANAGER_REJECTED", "TransferRequest", transferId, email,
                    "Manager rejected. Reason: " + request.getComments());

            // Notify the requester their transfer was rejected
            notificationService.send(
                    transfer.getRequestedBy().getId(),
                    "Transfer #" + transferId + " Rejected",
                    "Your request for " + transfer.getQuantity() + "x "
                            + transfer.getItem().getName()
                            + " was rejected. Reason: " + request.getComments(),
                    "TRANSFER_REJECTED");
        }

        return mapToResponse(transfer);
    }

    // ─── Head Office approve / reject ─────────────────────────────────────────

    @Transactional
    public TransferResponse headOfficeDecision(Long transferId,
            ApprovalDecisionRequest request,
            String email) {

        // Eager fetch so requestedBy / item / branches are available for notification
        TransferRequest transfer = getTransferWithDetails(transferId);
        assertStatus(transfer, TransferStatus.MANAGER_APPROVED);

        if (!request.isApproved() && isBlank(request.getComments())) {
            throw new RuntimeException("Rejection reason (comments) is mandatory");
        }

        User hoAdmin = getUser(email);

        if (request.isApproved()) {
            transfer.setStatus(TransferStatus.HO_APPROVED);
            transfer.setHoApprovedBy(hoAdmin);
            transfer.setHoApprovedAt(LocalDateTime.now());
            transfer.setHoComments(request.getComments());
            transferRequestRepository.save(transfer);

            auditLogService.log("HO_APPROVED", "TransferRequest", transferId, email,
                    "Head Office final approval granted");

            // Notify the requester of final HO approval
            notificationService.send(
                    transfer.getRequestedBy().getId(),
                    "Transfer #" + transferId + " Approved by Head Office",
                    "Your request for " + transfer.getQuantity() + "x "
                            + transfer.getItem().getName()
                            + " has received final approval from Head Office.",
                    "TRANSFER_APPROVED");

        } else {
            transfer.setStatus(TransferStatus.REJECTED);
            transfer.setHoApprovedBy(hoAdmin);
            transfer.setHoApprovedAt(LocalDateTime.now());
            transfer.setHoComments(request.getComments());
            transferRequestRepository.save(transfer);

            auditLogService.log("HO_REJECTED", "TransferRequest", transferId, email,
                    "Head Office rejected. Reason: " + request.getComments());

            // Notify the requester of HO rejection
            notificationService.send(
                    transfer.getRequestedBy().getId(),
                    "Transfer #" + transferId + " Rejected by Head Office",
                    "Your request for " + transfer.getQuantity() + "x "
                            + transfer.getItem().getName()
                            + " was rejected by Head Office. Reason: " + request.getComments(),
                    "TRANSFER_REJECTED");
        }

        return mapToResponse(transfer);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void assertStatus(TransferRequest transfer, TransferStatus expected) {
        if (transfer.getStatus() != expected) {
            throw new RuntimeException(
                    "Expected status " + expected + " but found: " + transfer.getStatus());
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** Plain fetch — safe for read-only operations with no notification. */
    private TransferRequest getTransfer(Long id) {
        return transferRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfer not found: " + id));
    }

    /**
     * Eager fetch — required wherever requestedBy/item/branches are needed for
     * notifications.
     */
    private TransferRequest getTransferWithDetails(Long id) {
        return transferRequestRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Transfer not found: " + id));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    private TransferResponse mapToResponse(TransferRequest t) {
        return TransferResponse.builder()
                .id(t.getId())
                .sourceBranchId(t.getSourceBranch().getId())
                .sourceBranchName(t.getSourceBranch().getName())
                .destinationBranchId(t.getDestinationBranch().getId())
                .destinationBranchName(t.getDestinationBranch().getName())
                .itemId(t.getItem().getId())
                .itemName(t.getItem().getName())
                .itemCode(t.getItem().getCode())
                .quantity(t.getQuantity())
                .totalValue(t.getTotalValue())
                .status(t.getStatus().name())
                .requiresHoApproval(t.isRequiresHoApproval())
                .managerComments(t.getManagerComments())
                .hoComments(t.getHoComments())
                .requestedAt(t.getRequestedAt())
                .build();
    }
}
