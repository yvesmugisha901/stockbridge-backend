package com.branch.inventory.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfer_costs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferCost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transfer_request_id", nullable = false, unique = true)
    private TransferRequest transferRequest;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(nullable = false, length = 50)
    private String costType;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recorded_by_id", nullable = false)
    private User recordedBy;

    @PrePersist
    protected void onCreate() {
        if (recordedAt == null)
            recordedAt = LocalDateTime.now();
    }
}
