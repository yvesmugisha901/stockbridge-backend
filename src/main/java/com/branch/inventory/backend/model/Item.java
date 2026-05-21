package com.branch.inventory.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Item entity — master catalogue of inventory items.
 *
 * FR-09: name, code, category, unit of measure
 * HO_ADMIN can add / edit / deactivate
 * FR-15: unitPrice used to calculate transfer total value
 * for approval tier routing
 */
@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** Unique item code e.g. "ITM-001". */
    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false)
    private String category;

    /** e.g. "kg", "litres", "pieces". */
    @Column(nullable = false)
    private String unitOfMeasure;

    /**
     * Unit price used to calculate total transfer value.
     * Determines whether HO approval is required (FR-15).
     */
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<StockLevel> stockLevels = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}