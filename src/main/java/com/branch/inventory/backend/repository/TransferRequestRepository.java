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
import java.util.Map;
import java.util.Optional;

@Repository
public interface TransferRequestRepository extends JpaRepository<TransferRequest, Long> {

        List<TransferRequest> findByStatus(TransferStatus status);

        Page<TransferRequest> findByStatus(TransferStatus status, Pageable pageable);

        Page<TransferRequest> findByStatusAndSourceBranch(TransferStatus status, Branch sourceBranch,
                        Pageable pageable);

        Page<TransferRequest> findByStatusAndDestinationBranch(TransferStatus status,
                        Branch destinationBranch, Pageable pageable);

        List<TransferRequest> findBySourceBranchId(Long branchId);

        List<TransferRequest> findByDestinationBranchId(Long branchId);

        List<TransferRequest> findByRequestedById(Long userId);

        Page<TransferRequest> findByRequestedBy(User requestedBy, Pageable pageable);

        List<TransferRequest> findBySourceBranchIdAndStatus(Long branchId, TransferStatus status);

        List<TransferRequest> findByStatusIn(List<TransferStatus> statuses);

        @Query("SELECT t FROM TransferRequest t WHERE " +
                        "t.sourceBranch.id = :branchId OR t.destinationBranch.id = :branchId")
        List<TransferRequest> findByBranch(@Param("branchId") Long branchId);

        @Query("SELECT t FROM TransferRequest t " +
                        "JOIN FETCH t.requestedBy " +
                        "JOIN FETCH t.item " +
                        "JOIN FETCH t.sourceBranch " +
                        "JOIN FETCH t.destinationBranch " +
                        "WHERE t.id = :id")
        Optional<TransferRequest> findByIdWithDetails(@Param("id") Long id);

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

        // ── FR-22: Finance transfers — JOIN FETCH branches so IDs are always loaded
        @Query(value = "SELECT DISTINCT t FROM TransferRequest t " +
                        "JOIN FETCH t.sourceBranch " +
                        "JOIN FETCH t.destinationBranch " +
                        "JOIN FETCH t.item " +
                        "WHERE (:branchId IS NULL OR t.sourceBranch.id = :branchId " +
                        "OR t.destinationBranch.id = :branchId) " +
                        "AND (:status IS NULL OR t.status = :status) " +
                        "AND (t.status IN :financeStatuses) " +
                        "AND (:from IS NULL OR CAST(t.requestedAt AS date) >= :from) " +
                        "AND (:to IS NULL OR CAST(t.requestedAt AS date) <= :to)", countQuery = "SELECT COUNT(DISTINCT t) FROM TransferRequest t "
                                        +
                                        "WHERE (:branchId IS NULL OR t.sourceBranch.id = :branchId " +
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

        @Query(value = """
                        SELECT
                            t.id                     AS id,
                            t.status                 AS type,
                            t.requested_at           AS timestamp,
                            u.full_name              AS user,
                            sb.name                  AS sourceBranch,
                            db.name                  AS destinationBranch,
                            i.name                   AS item,
                            t.quantity               AS quantity
                        FROM transfer_requests t
                        JOIN users u     ON t.requested_by_id        = u.id
                        JOIN branches sb ON t.source_branch_id       = sb.id
                        JOIN branches db ON t.destination_branch_id  = db.id
                        JOIN items i     ON t.item_id                = i.id
                        ORDER BY t.requested_at DESC
                        """, countQuery = "SELECT COUNT(*) FROM transfer_requests", nativeQuery = true)
        List<Map<String, Object>> findRecentActivity(Pageable pageable);

        Page<TransferRequest> findBySourceBranchAndStatusIn(
                        Branch sourceBranch, List<TransferStatus> statuses, Pageable pageable);

        Page<TransferRequest> findByDestinationBranchAndStatusIn(
                        Branch destinationBranch, List<TransferStatus> statuses, Pageable pageable);
}
