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

    @Transactional(readOnly = true)
    public Page<TransferResponse> getPendingForHeadOffice(Pageable pageable) {
        return transferRequestRepository
                .findByStatus(TransferStatus.MANAGER_APPROVED, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public TransferResponse managerDecision(Long transferId,
            ApprovalDecisionRequest request,
            String email) {
        TransferRequest transfer = getTransfer(transferId);
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

            auditLogService.log("MANAGER_APPROVED", "TransferRequest", transferId, email,
                    "Manager approved. Next status: " + nextStatus);
        } else {
            transfer.setStatus(TransferStatus.REJECTED);
            transfer.setManagerApprovedBy(manager);
            transfer.setManagerApprovedAt(LocalDateTime.now());
            transfer.setManagerComments(request.getComments());

            auditLogService.log("MANAGER_REJECTED", "TransferRequest", transferId, email,
                    "Manager rejected. Reason: " + request.getComments());
        }

        return mapToResponse(transferRequestRepository.save(transfer));
    }

    @Transactional
    public TransferResponse headOfficeDecision(Long transferId,
            ApprovalDecisionRequest request,
            String email) {
        TransferRequest transfer = getTransfer(transferId);
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

            auditLogService.log("HO_APPROVED", "TransferRequest", transferId, email,
                    "Head Office final approval granted");
        } else {
            transfer.setStatus(TransferStatus.REJECTED);
            transfer.setHoApprovedBy(hoAdmin);
            transfer.setHoApprovedAt(LocalDateTime.now());
            transfer.setHoComments(request.getComments());

            auditLogService.log("HO_REJECTED", "TransferRequest", transferId, email,
                    "Head Office rejected. Reason: " + request.getComments());
        }

        return mapToResponse(transferRequestRepository.save(transfer));
    }

    private void assertStatus(TransferRequest transfer, TransferStatus expected) {
        if (transfer.getStatus() != expected) {
            throw new RuntimeException(
                    "Expected status " + expected + " but found: " + transfer.getStatus());
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private TransferRequest getTransfer(Long id) {
        return transferRequestRepository.findById(id)
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