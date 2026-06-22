package com.branch.inventory.backend.service;

import com.branch.inventory.backend.dto.request.CreateTransferRequest;
import com.branch.inventory.backend.dto.response.TransferResponse;
import com.branch.inventory.backend.model.Branch;
import com.branch.inventory.backend.model.Item;
import com.branch.inventory.backend.model.StockLevel;
import com.branch.inventory.backend.model.TransferRequest;
import com.branch.inventory.backend.model.User;
import com.branch.inventory.backend.model.enums.Role;
import com.branch.inventory.backend.model.enums.TransferStatus;
import com.branch.inventory.backend.repository.BranchRepository;
import com.branch.inventory.backend.repository.ItemRepository;
import com.branch.inventory.backend.repository.StockLevelRepository;
import com.branch.inventory.backend.repository.TransferRequestRepository;
import com.branch.inventory.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferService {

        private final TransferRequestRepository transferRequestRepository;
        private final StockLevelRepository stockLevelRepository;
        private final BranchRepository branchRepository;
        private final ItemRepository itemRepository;
        private final UserRepository userRepository;
        private final AuditLogService auditLogService;
        private final NotificationService notificationService;

        @Value("${app.approval.quantity-threshold:100}")
        private int quantityThreshold;

        @Value("${app.approval.value-threshold:500000}")
        private BigDecimal valueThreshold;

        // ─── Create ──────────────────────────────────────────────────────────────

        @Transactional
        public TransferResponse createTransfer(CreateTransferRequest request, String email) {
                User requester = getUser(email);
                Branch sourceBranch = getBranch(request.getSourceBranchId());
                Branch destBranch = getBranch(request.getDestinationBranchId());
                Item item = getItem(request.getItemId());

                if (sourceBranch.getId().equals(destBranch.getId())) {
                        throw new RuntimeException("Source and destination branches must differ");
                }

                StockLevel stock = stockLevelRepository
                                .findByBranchIdAndItemId(sourceBranch.getId(), item.getId())
                                .orElseThrow(() -> new RuntimeException(
                                                "No stock record found for this item at the source branch"));

                int available = stock.getQuantityOnHand() - stock.getReservedQuantity();
                if (available < request.getQuantity()) {
                        throw new RuntimeException(
                                        "Insufficient stock. Available: " + available
                                                        + ", Requested: " + request.getQuantity());
                }

                BigDecimal totalValue = item.getUnitPrice()
                                .multiply(BigDecimal.valueOf(request.getQuantity()));

                boolean requiresHoApproval = request.getQuantity() >= quantityThreshold
                                || totalValue.compareTo(valueThreshold) >= 0;

                stock.setReservedQuantity(stock.getReservedQuantity() + request.getQuantity());
                stockLevelRepository.save(stock);

                TransferRequest transfer = TransferRequest.builder()
                                .sourceBranch(sourceBranch)
                                .destinationBranch(destBranch)
                                .item(item)
                                .quantity(request.getQuantity())
                                .totalValue(totalValue)
                                .justification(request.getJustification())
                                .requiresHoApproval(requiresHoApproval)
                                .status(TransferStatus.PENDING)
                                .requestedBy(requester)
                                .requestedAt(LocalDateTime.now())
                                .build();

                TransferRequest saved = transferRequestRepository.save(transfer);

                auditLogService.log("TRANSFER_CREATED", "TransferRequest", saved.getId(), email,
                                "Transfer request created. Requires HO approval: " + requiresHoApproval);

                String notifTitle = "Transfer Request #" + saved.getId() + " Needs Approval";
                String notifMessage = requester.getFullName() + " requested " + request.getQuantity()
                                + "x " + item.getName() + " from " + sourceBranch.getName()
                                + " → " + destBranch.getName() + ".";

                if (requiresHoApproval) {
                        // Large/high-value transfer: notify all HO admins
                        userRepository.findByRole(Role.HO_ADMIN)
                                        .forEach(admin -> notificationService.send(
                                                        admin.getId(), notifTitle, notifMessage, "TRANSFER_PENDING"));
                } else {
                        // FIX: notify managers of the DESTINATION branch (the branch requesting stock),
                        // not the source branch. The destination branch manager owns this approval —
                        // they are requesting stock for their branch and need to confirm/approve it.
                        userRepository.findByBranchIdAndRole(destBranch.getId(), Role.MANAGER)
                                        .forEach(manager -> notificationService.send(
                                                        manager.getId(), notifTitle, notifMessage, "TRANSFER_PENDING"));
                }

                return mapToResponse(saved);
        }

        // ─── Read ─────────────────────────────────────────────────────────────────

        @Transactional(readOnly = true)
        public Page<TransferResponse> getTransfers(String email, Long branchId, String status,
                        Long itemId, String fromDate, String toDate, Pageable pageable) {

                User user = getUser(email);
                LocalDate from = fromDate != null ? LocalDate.parse(fromDate) : null;
                LocalDate to = toDate != null ? LocalDate.parse(toDate) : null;
                TransferStatus transferStatus = status != null ? TransferStatus.valueOf(status) : null;

                User filterUser = null;
                Long filterBranchId = branchId;

                if (user.getRole() == Role.STAFF) {
                        filterUser = user;
                        filterBranchId = null;
                } else if (user.getRole() == Role.MANAGER) {
                        filterUser = null;
                        if (filterBranchId == null && user.getBranch() != null) {
                                filterBranchId = user.getBranch().getId();
                        }
                } else {
                        filterUser = null;
                }

                return transferRequestRepository
                                .findByFilters(filterUser, filterBranchId, transferStatus,
                                                itemId, from, to, pageable)
                                .map(this::mapToResponse);
        }

        @Transactional(readOnly = true)
        public Page<TransferResponse> getMyTransfers(String email, Pageable pageable) {
                User user = getUser(email);
                return transferRequestRepository
                                .findByRequestedBy(user, pageable)
                                .map(this::mapToResponse);
        }

        @Transactional(readOnly = true)
        public TransferResponse getTransferById(Long id, String email) {
                return mapToResponse(getTransfer(id));
        }

        // ─── Status Transitions ───────────────────────────────────────────────────

        @Transactional
        public TransferResponse markInTransit(Long id, String email) {
                TransferRequest transfer = getTransferWithDetails(id);
                assertStatus(transfer, TransferStatus.HO_APPROVED, TransferStatus.MANAGER_APPROVED);

                transfer.setStatus(TransferStatus.IN_TRANSIT);
                transfer.setDispatchedAt(LocalDateTime.now());
                transferRequestRepository.save(transfer);

                auditLogService.log("MARKED_IN_TRANSIT", "TransferRequest", id, email, "Stock dispatched");

                notificationService.send(
                                transfer.getRequestedBy().getId(),
                                "Transfer #" + id + " Is On Its Way",
                                transfer.getQuantity() + "x " + transfer.getItem().getName()
                                                + " has been dispatched from "
                                                + transfer.getSourceBranch().getName() + ".",
                                "TRANSFER_IN_TRANSIT");

                return mapToResponse(transfer);
        }

        @Transactional
        public TransferResponse confirmReceipt(Long id, String email) {
                TransferRequest transfer = getTransferWithDetails(id);
                assertStatus(transfer, TransferStatus.IN_TRANSIT);

                StockLevel sourceStock = stockLevelRepository
                                .findByBranchIdAndItemId(
                                                transfer.getSourceBranch().getId(), transfer.getItem().getId())
                                .orElseThrow(() -> new RuntimeException("Source stock record not found"));
                sourceStock.setReservedQuantity(
                                sourceStock.getReservedQuantity() - transfer.getQuantity());
                sourceStock.setQuantityOnHand(
                                sourceStock.getQuantityOnHand() - transfer.getQuantity());
                sourceStock.setLastUpdated(LocalDateTime.now());
                stockLevelRepository.save(sourceStock);

                StockLevel destStock = stockLevelRepository
                                .findByBranchIdAndItemId(
                                                transfer.getDestinationBranch().getId(),
                                                transfer.getItem().getId())
                                .orElseGet(() -> StockLevel.builder()
                                                .branch(transfer.getDestinationBranch())
                                                .item(transfer.getItem())
                                                .quantityOnHand(0)
                                                .reservedQuantity(0)
                                                .minimumThreshold(0)
                                                .lastUpdated(LocalDateTime.now())
                                                .build());
                destStock.setQuantityOnHand(destStock.getQuantityOnHand() + transfer.getQuantity());
                destStock.setLastUpdated(LocalDateTime.now());
                stockLevelRepository.save(destStock);

                transfer.setStatus(TransferStatus.COMPLETED);
                transfer.setReceivedAt(LocalDateTime.now());
                transferRequestRepository.save(transfer);

                auditLogService.log("RECEIPT_CONFIRMED", "TransferRequest", id, email,
                                "Stock received. Levels updated on both branches.");

                notificationService.send(
                                transfer.getRequestedBy().getId(),
                                "Transfer #" + id + " Completed",
                                transfer.getQuantity() + "x " + transfer.getItem().getName()
                                                + " has been received at "
                                                + transfer.getDestinationBranch().getName() + ".",
                                "TRANSFER_RECEIVED");

                return mapToResponse(transfer);
        }

        @Transactional
        public TransferResponse cancelTransfer(Long id, String email) {
                TransferRequest transfer = getTransfer(id);
                assertStatus(transfer, TransferStatus.PENDING);

                StockLevel stock = stockLevelRepository
                                .findByBranchIdAndItemId(
                                                transfer.getSourceBranch().getId(), transfer.getItem().getId())
                                .orElseThrow(() -> new RuntimeException("Source stock record not found"));
                stock.setReservedQuantity(stock.getReservedQuantity() - transfer.getQuantity());
                stockLevelRepository.save(stock);

                transfer.setStatus(TransferStatus.CANCELLED);
                transferRequestRepository.save(transfer);

                auditLogService.log("TRANSFER_CANCELLED", "TransferRequest", id, email,
                                "Cancelled by requester");
                return mapToResponse(transfer);
        }

        // ─── Approval ─────────────────────────────────────────────────────────────

        @Transactional
        public TransferResponse approveTransfer(Long id, String email) {
                TransferRequest transfer = getTransferWithDetails(id);
                assertStatus(transfer, TransferStatus.PENDING);

                TransferStatus newStatus = transfer.isRequiresHoApproval()
                                ? TransferStatus.HO_APPROVED
                                : TransferStatus.MANAGER_APPROVED;

                transfer.setStatus(newStatus);
                transferRequestRepository.save(transfer);

                auditLogService.log("TRANSFER_APPROVED", "TransferRequest", id, email, "Approved");

                notificationService.send(
                                transfer.getRequestedBy().getId(),
                                "Transfer #" + id + " Approved",
                                "Your request for " + transfer.getQuantity() + "x "
                                                + transfer.getItem().getName() + " has been approved.",
                                "TRANSFER_APPROVED");

                return mapToResponse(transfer);
        }

        @Transactional
        public TransferResponse rejectTransfer(Long id, String email, String reason) {
                TransferRequest transfer = getTransferWithDetails(id);
                assertStatus(transfer, TransferStatus.PENDING);

                StockLevel stock = stockLevelRepository
                                .findByBranchIdAndItemId(
                                                transfer.getSourceBranch().getId(), transfer.getItem().getId())
                                .orElseThrow(() -> new RuntimeException("Source stock record not found"));
                stock.setReservedQuantity(stock.getReservedQuantity() - transfer.getQuantity());
                stockLevelRepository.save(stock);

                transfer.setStatus(TransferStatus.REJECTED);
                transferRequestRepository.save(transfer);

                auditLogService.log("TRANSFER_REJECTED", "TransferRequest", id, email,
                                "Rejected. Reason: " + reason);

                notificationService.send(
                                transfer.getRequestedBy().getId(),
                                "Transfer #" + id + " Rejected",
                                "Your request for " + transfer.getQuantity() + "x "
                                                + transfer.getItem().getName()
                                                + " was rejected. Reason: " + reason,
                                "TRANSFER_REJECTED");

                return mapToResponse(transfer);
        }

        // ─── Ready to dispatch / Incoming ─────────────────────────────────────────

        @Transactional(readOnly = true)
        public Page<TransferResponse> getReadyToDispatch(String email, Pageable pageable) {
                User user = getUser(email);
                if (user.getBranch() == null)
                        throw new RuntimeException("User is not assigned to a branch");
                return transferRequestRepository
                                .findBySourceBranchAndStatusIn(
                                                user.getBranch(),
                                                List.of(TransferStatus.HO_APPROVED,
                                                                TransferStatus.MANAGER_APPROVED),
                                                pageable)
                                .map(this::mapToResponse);
        }

        @Transactional(readOnly = true)
        public Page<TransferResponse> getIncomingTransfers(String email, Pageable pageable) {
                User user = getUser(email);
                if (user.getBranch() == null)
                        throw new RuntimeException("User is not assigned to a branch");
                return transferRequestRepository
                                .findByDestinationBranchAndStatusIn(
                                                user.getBranch(),
                                                List.of(TransferStatus.IN_TRANSIT),
                                                pageable)
                                .map(this::mapToResponse);
        }

        // ─── Helpers ──────────────────────────────────────────────────────────────

        private void assertStatus(TransferRequest transfer, TransferStatus... allowed) {
                for (TransferStatus s : allowed) {
                        if (transfer.getStatus() == s)
                                return;
                }
                throw new RuntimeException("Invalid status transition from: " + transfer.getStatus());
        }

        private TransferRequest getTransfer(Long id) {
                return transferRequestRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Transfer not found: " + id));
        }

        private TransferRequest getTransferWithDetails(Long id) {
                return transferRequestRepository.findByIdWithDetails(id)
                                .orElseThrow(() -> new RuntimeException("Transfer not found: " + id));
        }

        private User getUser(String email) {
                return userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        }

        private Branch getBranch(Long id) {
                return branchRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Branch not found: " + id));
        }

        private Item getItem(Long id) {
                return itemRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Item not found: " + id));
        }

        // ─── Package-private so ReportService can reuse it ────────────────────────
        TransferResponse mapToResponse(TransferRequest t) {
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
                                .justification(t.getJustification())
                                .status(t.getStatus().name())
                                .requiresHoApproval(t.isRequiresHoApproval())
                                // Provides both email and full name so the frontend can display either
                                .requestedByEmail(t.getRequestedBy().getEmail())
                                .requestedByName(t.getRequestedBy().getFullName()) // FIX: was missing — caused null in
                                                                                   // dashboard
                                .requestedAt(t.getRequestedAt())
                                .dispatchedAt(t.getDispatchedAt())
                                .receivedAt(t.getReceivedAt())
                                .build();
        }
}
