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
        private final NotificationService notificationService;
        // FIX: inject TransferService so we reuse its complete mapToResponse
        // and never have a second incomplete mapping diverge again.
        private final TransferService transferService;

        // ─── Manager queue ────────────────────────────────────────────────────────

        @Transactional(readOnly = true)
        public Page<TransferResponse> getPendingForManager(String email, Pageable pageable) {
                User manager = getUser(email);
                if (manager.getBranch() == null)
                        throw new RuntimeException("Manager is not assigned to a branch");
                return transferRequestRepository
                                .findByStatusAndDestinationBranch(TransferStatus.PENDING, manager.getBranch(), pageable)
                                .map(transferService::mapToResponse); // ← delegate, not local mapToResponse
        }

        // ─── HO queue ─────────────────────────────────────────────────────────────

        @Transactional(readOnly = true)
        public Page<TransferResponse> getPendingForHeadOffice(Pageable pageable) {
                return transferRequestRepository
                                .findByStatus(TransferStatus.MANAGER_APPROVED, pageable)
                                .map(transferService::mapToResponse);
        }

        // ─── Source branch dispatch queue ─────────────────────────────────────────

        @Transactional(readOnly = true)
        public Page<TransferResponse> getPendingDispatch(String email, Pageable pageable) {
                User manager = getUser(email);
                if (manager.getBranch() == null)
                        throw new RuntimeException("Manager is not assigned to a branch");
                return transferRequestRepository
                                .findByStatusAndSourceBranch(TransferStatus.HO_APPROVED, manager.getBranch(), pageable)
                                .map(transferService::mapToResponse);
        }

        // ─── Manager approve / reject ─────────────────────────────────────────────

        @Transactional
        public TransferResponse managerDecision(Long transferId,
                        ApprovalDecisionRequest request,
                        String email) {

                TransferRequest transfer = getTransferWithDetails(transferId);
                assertStatus(transfer, TransferStatus.PENDING);

                if (!request.isApproved() && isBlank(request.getComments()))
                        throw new RuntimeException("Rejection reason (comments) is mandatory");

                User manager = getUser(email);

                if (manager.getBranch() == null ||
                                !manager.getBranch().getId().equals(transfer.getDestinationBranch().getId()))
                        throw new RuntimeException(
                                        "You are not the manager of the branch that requested this transfer");

                if (request.isApproved()) {
                        transfer.setStatus(TransferStatus.MANAGER_APPROVED);
                        transfer.setManagerApprovedBy(manager);
                        transfer.setManagerApprovedAt(LocalDateTime.now());
                        transfer.setManagerComments(request.getComments());
                        transferRequestRepository.save(transfer);

                        auditLogService.log("MANAGER_APPROVED", "TransferRequest", transferId, email,
                                        "Manager approved. Forwarded to HO Admin.");

                        userRepository.findByRole(com.branch.inventory.backend.model.enums.Role.HO_ADMIN)
                                        .forEach(admin -> notificationService.send(
                                                        admin.getId(),
                                                        "Transfer #" + transferId + " Awaiting HO Approval",
                                                        transfer.getQuantity() + "x " + transfer.getItem().getName()
                                                                        + " from "
                                                                        + transfer.getSourceBranch().getName()
                                                                        + " to "
                                                                        + transfer.getDestinationBranch().getName()
                                                                        + " approved by branch manager. Needs your review.",
                                                        "TRANSFER_PENDING"));

                        notificationService.send(
                                        transfer.getRequestedBy().getId(),
                                        "Transfer #" + transferId + " Approved by Manager",
                                        "Your request for " + transfer.getQuantity() + "x "
                                                        + transfer.getItem().getName()
                                                        + " has been approved by your branch manager and is awaiting Head Office review.",
                                        "TRANSFER_APPROVED");

                } else {
                        transfer.setStatus(TransferStatus.REJECTED);
                        transfer.setManagerApprovedBy(manager);
                        transfer.setManagerApprovedAt(LocalDateTime.now());
                        transfer.setManagerComments(request.getComments());
                        transferRequestRepository.save(transfer);

                        auditLogService.log("MANAGER_REJECTED", "TransferRequest", transferId, email,
                                        "Manager rejected. Reason: " + request.getComments());

                        notificationService.send(
                                        transfer.getRequestedBy().getId(),
                                        "Transfer #" + transferId + " Rejected",
                                        "Your request for " + transfer.getQuantity() + "x "
                                                        + transfer.getItem().getName()
                                                        + " was rejected by your branch manager. Reason: "
                                                        + request.getComments(),
                                        "TRANSFER_REJECTED");
                }

                return transferService.mapToResponse(transfer);
        }

        // ─── Head Office approve / reject ─────────────────────────────────────────

        @Transactional
        public TransferResponse headOfficeDecision(Long transferId,
                        ApprovalDecisionRequest request,
                        String email) {

                TransferRequest transfer = getTransferWithDetails(transferId);
                assertStatus(transfer, TransferStatus.MANAGER_APPROVED);

                if (!request.isApproved() && isBlank(request.getComments()))
                        throw new RuntimeException("Rejection reason (comments) is mandatory");

                User hoAdmin = getUser(email);

                if (request.isApproved()) {
                        transfer.setStatus(TransferStatus.HO_APPROVED);
                        transfer.setHoApprovedBy(hoAdmin);
                        transfer.setHoApprovedAt(LocalDateTime.now());
                        transfer.setHoComments(request.getComments());
                        transferRequestRepository.save(transfer);

                        auditLogService.log("HO_APPROVED", "TransferRequest", transferId, email,
                                        "Head Office approved. Forwarded to source branch manager for dispatch.");

                        notificationService.send(
                                        transfer.getRequestedBy().getId(),
                                        "Transfer #" + transferId + " Approved by Head Office",
                                        "Your request for " + transfer.getQuantity() + "x "
                                                        + transfer.getItem().getName()
                                                        + " has received final approval from Head Office. "
                                                        + transfer.getSourceBranch().getName()
                                                        + " will now dispatch the stock.",
                                        "TRANSFER_APPROVED");

                        userRepository.findByBranchIdAndRole(
                                        transfer.getSourceBranch().getId(),
                                        com.branch.inventory.backend.model.enums.Role.MANAGER)
                                        .forEach(sourceMgr -> notificationService.send(
                                                        sourceMgr.getId(),
                                                        "Dispatch Required — Transfer #" + transferId,
                                                        transfer.getQuantity() + "x " + transfer.getItem().getName()
                                                                        + " needs to be dispatched to "
                                                                        + transfer.getDestinationBranch().getName()
                                                                        + ". Please confirm and mark as In Transit.",
                                                        "TRANSFER_DISPATCH"));

                } else {
                        transfer.setStatus(TransferStatus.REJECTED);
                        transfer.setHoApprovedBy(hoAdmin);
                        transfer.setHoApprovedAt(LocalDateTime.now());
                        transfer.setHoComments(request.getComments());
                        transferRequestRepository.save(transfer);

                        auditLogService.log("HO_REJECTED", "TransferRequest", transferId, email,
                                        "Head Office rejected. Reason: " + request.getComments());

                        notificationService.send(
                                        transfer.getRequestedBy().getId(),
                                        "Transfer #" + transferId + " Rejected by Head Office",
                                        "Your request for " + transfer.getQuantity() + "x "
                                                        + transfer.getItem().getName()
                                                        + " was rejected by Head Office. Reason: "
                                                        + request.getComments(),
                                        "TRANSFER_REJECTED");
                }

                return transferService.mapToResponse(transfer);
        }

        // ─── Dispatch ─────────────────────────────────────────────────────────────

        @Transactional
        public TransferResponse dispatchTransfer(Long transferId, String email) {

                TransferRequest transfer = getTransferWithDetails(transferId);
                assertStatus(transfer, TransferStatus.HO_APPROVED);

                User manager = getUser(email);

                if (manager.getBranch() == null ||
                                !manager.getBranch().getId().equals(transfer.getSourceBranch().getId()))
                        throw new RuntimeException(
                                        "You are not the manager of the branch supplying this transfer");

                transfer.setStatus(TransferStatus.IN_TRANSIT);
                transfer.setDispatchedBy(manager);
                transfer.setDispatchedAt(LocalDateTime.now());
                transferRequestRepository.save(transfer);

                auditLogService.log("DISPATCHED", "TransferRequest", transferId, email,
                                "Stock dispatched by source branch manager. Now IN_TRANSIT.");

                notificationService.send(
                                transfer.getRequestedBy().getId(),
                                "Transfer #" + transferId + " Dispatched — Stock In Transit",
                                transfer.getQuantity() + "x " + transfer.getItem().getName()
                                                + " has been dispatched from " + transfer.getSourceBranch().getName()
                                                + " and is on the way to your branch.",
                                "TRANSFER_IN_TRANSIT");

                userRepository.findByBranchIdAndRole(
                                transfer.getDestinationBranch().getId(),
                                com.branch.inventory.backend.model.enums.Role.MANAGER)
                                .forEach(destMgr -> notificationService.send(
                                                destMgr.getId(),
                                                "Stock In Transit — Transfer #" + transferId,
                                                transfer.getQuantity() + "x " + transfer.getItem().getName()
                                                                + " is on its way from "
                                                                + transfer.getSourceBranch().getName()
                                                                + ". Please confirm receipt when it arrives.",
                                                "TRANSFER_IN_TRANSIT"));

                return transferService.mapToResponse(transfer);
        }

        // ─── Helpers ──────────────────────────────────────────────────────────────

        private void assertStatus(TransferRequest transfer, TransferStatus expected) {
                if (transfer.getStatus() != expected)
                        throw new RuntimeException(
                                        "Expected status " + expected + " but found: " + transfer.getStatus());
        }

        private boolean isBlank(String s) {
                return s == null || s.trim().isEmpty();
        }

        private TransferRequest getTransferWithDetails(Long id) {
                return transferRequestRepository.findByIdWithDetails(id)
                                .orElseThrow(() -> new RuntimeException("Transfer not found: " + id));
        }

        private User getUser(String email) {
                return userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        }
}
