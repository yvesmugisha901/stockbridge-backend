package com.branch.inventory.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * StockLevel entity — tracks quantity of one item at one branch.
 *
 * FR-10: quantityOnHand, reservedQuantity, minimumThreshold per branch per item
 * FR-11: quantityOnHand updated by manual adjustments
 * FR-12: alert fired when quantityOnHand <= minimumThreshold
 * FR-19: quantityOnHand updated automatically on transfer receipt
 */
@Entity
@Table(name = "stock_levels", uniqueConstraints = @UniqueConstraint(columnNames = { "branch_id", "item_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    /** Total physical units currently at this branch. */
    @Column(nullable = false)
    @Builder.Default
    private int quantityOnHand = 0;

    /**
     * Units reserved for approved outbound transfers.
     * Available stock = quantityOnHand - reservedQuantity.
     */
    @Column(nullable = false)
    @Builder.Default
    private int reservedQuantity = 0;

    /**
     * Low-stock alert is triggered when quantityOnHand
     * falls at or below this value (FR-12).
     */
    @Column(nullable = false)
    @Builder.Default
    private int minimumThreshold = 0;

    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}