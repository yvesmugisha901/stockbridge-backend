package com.branch.inventory.backend.service;

import com.branch.inventory.backend.dto.request.TransferCostRequest;
import com.branch.inventory.backend.dto.response.FinanceSummaryResponse;
import com.branch.inventory.backend.dto.response.TransferCostResponse;
import com.branch.inventory.backend.model.TransferCost;
import com.branch.inventory.backend.model.TransferRequest;
import com.branch.inventory.backend.model.User;
import com.branch.inventory.backend.model.enums.TransferStatus;
import com.branch.inventory.backend.repository.TransferCostRepository;
import com.branch.inventory.backend.repository.TransferRequestRepository;
import com.branch.inventory.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final TransferRequestRepository transferRequestRepository;
    private final TransferCostRepository transferCostRepository;
    private final UserRepository userRepository;

    // ── Helpers ───────────────────────────────────────────────────────────────

    private TransferRequest getTransfer(Long id) {
        return transferRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfer not found: " + id));
    }

    /** Resolves the currently authenticated user from the security context. */
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + email));
    }

    private TransferCostResponse mapToTransferCostResponse(TransferRequest t) {
        TransferCost cost = transferCostRepository.findByTransferRequestId(t.getId()).orElse(null);

        return TransferCostResponse.builder()
                .transferId(t.getId())
                .sourceBranchId(t.getSourceBranch().getId())
                .sourceBranchName(t.getSourceBranch().getName())
                .destinationBranchId(t.getDestinationBranch().getId())
                .destinationBranchName(t.getDestinationBranch().getName())
                .itemName(t.getItem().getName())
                .quantity(t.getQuantity())
                .transferValue(t.getTotalValue())
                .status(t.getStatus().name())
                .costAmount(cost != null ? cost.getAmount() : null)
                .currency(cost != null ? cost.getCurrency() : null)
                .costType(cost != null ? cost.getCostType() : null)
                .costNotes(cost != null ? cost.getNotes() : null)
                .requestedAt(t.getRequestedAt())
                .build();
    }

    // ── FR-22: View approved transfers ────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<TransferCostResponse> getApprovedTransfers(
            Long branchId, String fromDate, String toDate, String status, Pageable pageable) {

        LocalDate from = fromDate != null ? LocalDate.parse(fromDate) : null;
        LocalDate to = toDate != null ? LocalDate.parse(toDate) : null;

        List<TransferStatus> financeStatuses = List.of(
                TransferStatus.HO_APPROVED,
                TransferStatus.IN_TRANSIT,
                TransferStatus.RECEIVED,
                TransferStatus.COMPLETED);

        TransferStatus filteredStatus = status != null ? TransferStatus.valueOf(status) : null;

        return transferRequestRepository
                .findByFinanceFilters(branchId, filteredStatus, financeStatuses, from, to, pageable)
                .map(this::mapToTransferCostResponse);
    }

    @Transactional(readOnly = true)
    public TransferCostResponse getTransferCostDetails(Long transferId) {
        return mapToTransferCostResponse(getTransfer(transferId));
    }

    // ── FR-23: Record transfer cost ───────────────────────────────────────────

    @Transactional
    public TransferCostResponse recordTransferCost(Long transferId, TransferCostRequest request) {
        TransferRequest transfer = getTransfer(transferId);

        if (transferCostRepository.existsByTransferRequestId(transferId)) {
            throw new RuntimeException("Cost record already exists. Use PUT to update.");
        }

        User currentUser = getCurrentUser();

        TransferCost cost = TransferCost.builder()
                .transferRequest(transfer)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .costType(request.getCostType())
                .notes(request.getNotes())
                .recordedBy(currentUser) // ← was missing; caused the 500
                .recordedAt(LocalDateTime.now())
                .build();

        transferCostRepository.save(cost);
        return mapToTransferCostResponse(transfer);
    }

    // ── FR-23: Update transfer cost ───────────────────────────────────────────

    @Transactional
    public TransferCostResponse updateTransferCost(Long transferId, TransferCostRequest request) {
        TransferCost cost = transferCostRepository.findByTransferRequestId(transferId)
                .orElseThrow(() -> new RuntimeException("No cost record found. Use POST to create."));

        if (request.getAmount() != null)
            cost.setAmount(request.getAmount());
        if (request.getCurrency() != null)
            cost.setCurrency(request.getCurrency());
        if (request.getCostType() != null)
            cost.setCostType(request.getCostType());
        if (request.getNotes() != null)
            cost.setNotes(request.getNotes());

        transferCostRepository.save(cost);
        return mapToTransferCostResponse(cost.getTransferRequest());
    }

    // ── FR-24: Finance summary ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public FinanceSummaryResponse getFinanceSummary(Long branchId, String fromDate, String toDate) {
        LocalDate from = fromDate != null ? LocalDate.parse(fromDate) : null;
        LocalDate to = toDate != null ? LocalDate.parse(toDate) : null;

        BigDecimal totalCost = transferCostRepository.sumCostsByFilters(branchId, from, to);
        long totalTransfers = transferCostRepository.countByFilters(branchId, from, to);

        return FinanceSummaryResponse.builder()
                .totalTransfers(totalTransfers)
                .totalCost(totalCost != null ? totalCost : BigDecimal.ZERO)
                .branchId(branchId)
                .fromDate(from)
                .toDate(to)
                .build();
    }
}
