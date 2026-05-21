package com.branch.inventory.backend.model;

import com.branch.inventory.backend.model.enums.TransferStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Approval entity — records each individual approval decision.
 *
 * FR-16: Branch Manager first-level decision
 * FR-17: HO_ADMIN second-level decision
 * FR-20: Rejection with mandatory comments
 * FR-21: Full audit trail — every decision stored with actor, timestamp,
 * comments
 *
 * One TransferRequest can have up to two Approval records
 * (one per tier). This gives a full immutable decision history.
 */
@Entity
@Table(name = "approvals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Approval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transfer_request_id", nullable = false)
    private TransferRequest transferRequest;

    /**
     * Approval tier:
     * MANAGER_APPROVED = first-level decision
     * HO_APPROVED = second-level decision
     * REJECTED = rejection at either tier
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus decision;

    /** The user who made this approval decision. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "decided_by", nullable = false)
    private User decidedBy;

    @Column(nullable = false)
    private LocalDateTime decidedAt;

    /**
     * Mandatory on rejection; optional on approval.
     * FR-16, FR-17: comments required when rejecting.
     */
    @Column(length = 500)
    private String comments;

    @PrePersist
    protected void onCreate() {
        if (decidedAt == null)
            decidedAt = LocalDateTime.now();
    }
}