package com.branch.inventory.backend.repository;

import com.branch.inventory.backend.model.Branch;
import com.branch.inventory.backend.model.TransferRequest;
import com.branch.inventory.backend.model.User;
import com.branch.inventory.backend.model.enums.TransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransferRequestRepository extends JpaRepository<TransferRequest, Long> {

        List<TransferRequest> findByStatus(TransferStatus status);

        // Used by ApprovalService.getPendingForHeadOffice()
        Page<TransferRequest> findByStatus(TransferStatus status, Pageable pageable);

        // Used by ApprovalService.getPendingForManager()
        Page<TransferRequest> findByStatusAndSourceBranch(TransferStatus status, Branch sourceBranch,
                        Pageable pageable);

        List<TransferRequest> findBySourceBranchId(Long branchId);

        List<TransferRequest> findByDestinationBranchId(Long branchId);

        List<TransferRequest> findByRequestedById(Long userId);

        // Used by TransferService.getMyTransfers()
        Page<TransferRequest> findByRequestedBy(User requestedBy, Pageable pageable);

        List<TransferRequest> findBySourceBranchIdAndStatus(Long branchId, TransferStatus status);

        List<TransferRequest> findByStatusIn(List<TransferStatus> statuses);

        @Query("SELECT t FROM TransferRequest t WHERE " +
                        "t.sourceBranch.id = :branchId OR t.destinationBranch.id = :branchId")
        List<TransferRequest> findByBranch(@Param("branchId") Long branchId);

        // Used by TransferService.getTransfers()
        @Query("SELECT t FROM TransferRequest t WHERE " +
                        "(:user IS NULL OR t.requestedBy = :user) " +
                        "AND (:branchId IS NULL OR t.sourceBranch.id = :branchId OR t.destinationBranch.id = :branchId) "
                        +
                        "AND (:status IS NULL OR t.status = :status) " +
                        "AND (:itemId IS NULL OR t.item.id = :itemId) " +
                        "AND (:from IS NULL OR CAST(t.requestedAt AS date) >= :from) " +
                        "AND (:to IS NULL OR CAST(t.requestedAt AS date) <= :to)")
        Page<TransferRequest> findByFilters(
                        @Param("user") User user,
                        @Param("branchId") Long branchId,
                        @Param("status") TransferStatus status,
                        @Param("itemId") Long itemId,
                        @Param("from") LocalDate from,
                        @Param("to") LocalDate to,
                        Pageable pageable);

        // Used by ReportService.getTransferHistoryReport()
        @Query("SELECT t FROM TransferRequest t WHERE " +
                        "(:branchId IS NULL OR t.sourceBranch.id = :branchId OR t.destinationBranch.id = :branchId) " +
                        "AND (:status IS NULL OR t.status = :status) " +
                        "AND (:itemId IS NULL OR t.item.id = :itemId) " +
                        "AND (:from IS NULL OR CAST(t.requestedAt AS date) >= :from) " +
                        "AND (:to IS NULL OR CAST(t.requestedAt AS date) <= :to)")
        List<TransferRequest> findForHistoryReport(
                        @Param("branchId") Long branchId,
                        @Param("status") TransferStatus status,
                        @Param("itemId") Long itemId,
                        @Param("from") LocalDate from,
                        @Param("to") LocalDate to);

        @Query("SELECT t FROM TransferRequest t WHERE " +
                        "(:branchId IS NULL OR t.sourceBranch.id = :branchId " +
                        "OR t.destinationBranch.id = :branchId) " +
                        "AND (:status IS NULL OR t.status = :status) " +
                        "AND (t.status IN :financeStatuses) " +
                        "AND (:from IS NULL OR CAST(t.requestedAt AS date) >= :from) " +
                        "AND (:to IS NULL OR CAST(t.requestedAt AS date) <= :to)")
        Page<TransferRequest> findByFinanceFilters(
                        @Param("branchId") Long branchId,
                        @Param("status") TransferStatus status,
                        @Param("financeStatuses") List<TransferStatus> financeStatuses,
                        @Param("from") LocalDate from,
                        @Param("to") LocalDate to,
                        Pageable pageable);
}