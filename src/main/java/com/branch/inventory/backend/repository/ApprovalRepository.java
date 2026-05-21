package com.branch.inventory.backend.repository;

import com.branch.inventory.backend.model.Approval;
import com.branch.inventory.backend.model.enums.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    // All approval decisions for a specific transfer
    List<Approval> findByTransferRequestId(Long transferRequestId);

    // All decisions made by a specific user — decidedBy is a User object, so use
    // decidedBy_Id
    List<Approval> findByDecidedById(Long decidedById);

    // All approvals with a specific decision (e.g. REJECTED, HO_APPROVED)
    List<Approval> findByDecision(TransferStatus decision);

    // All decisions for a transfer ordered by time — useful for audit trail display
    List<Approval> findByTransferRequestIdOrderByDecidedAtAsc(Long transferRequestId);

    // Check if a user already acted on a specific transfer
    boolean existsByTransferRequestIdAndDecidedById(Long transferRequestId, Long decidedById);
}