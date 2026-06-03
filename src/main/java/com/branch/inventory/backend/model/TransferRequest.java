package com.branch.inventory.backend.model;

import com.branch.inventory.backend.model.enums.TransferStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transfer_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_branch_id", nullable = false)
    private Branch sourceBranch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_branch_id", nullable = false)
    private Branch destinationBranch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalValue;

    @Column(nullable = false, length = 500)
    private String justification;

    @Column(nullable = false)
    @Builder.Default
    private boolean requiresHoApproval = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TransferStatus status = TransferStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private User requestedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    // ── Level 1: Branch A manager ─────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_approved_by_id")
    private User managerApprovedBy;

    private LocalDateTime managerApprovedAt;

    @Column(length = 500)
    private String managerComments;

    // ── Level 2: HO Admin ─────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ho_approved_by_id")
    private User hoApprovedBy;

    private LocalDateTime hoApprovedAt;

    @Column(length = 500)
    private String hoComments;

    // ── Level 3: Branch B manager dispatches ──────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispatched_by_id")
    private User dispatchedBy;

    private LocalDateTime dispatchedAt;

    // ── Level 4: Branch A confirms receipt ────────────────────────────────────
    private LocalDateTime receivedAt;

    // ── Audit timestamps ──────────────────────────────────────────────────────
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ── Relationships ─────────────────────────────────────────────────────────
    @OneToMany(mappedBy = "transferRequest", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Approval> approvals = new ArrayList<>();

    @OneToOne(mappedBy = "transferRequest", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private TransferCost transferCost;

    // ── Lifecycle hooks ───────────────────────────────────────────────────────
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (requestedAt == null)
            requestedAt = now;
        if (createdAt == null)
            createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
