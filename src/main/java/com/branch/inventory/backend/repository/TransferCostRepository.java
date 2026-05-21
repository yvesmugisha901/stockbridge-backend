package com.branch.inventory.backend.repository;

import com.branch.inventory.backend.model.TransferCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TransferCostRepository extends JpaRepository<TransferCost, Long> {

        Optional<TransferCost> findByTransferRequestId(Long transferRequestId);

        boolean existsByTransferRequestId(Long transferRequestId);

        @Query("SELECT SUM(tc.amount) FROM TransferCost tc " +
                        "WHERE (:branchId IS NULL OR tc.transferRequest.sourceBranch.id = :branchId " +
                        "OR tc.transferRequest.destinationBranch.id = :branchId) " +
                        "AND (:from IS NULL OR CAST(tc.recordedAt AS date) >= :from) " +
                        "AND (:to IS NULL OR CAST(tc.recordedAt AS date) <= :to)")
        BigDecimal sumCostsByFilters(@Param("branchId") Long branchId,
                        @Param("from") LocalDate from,
                        @Param("to") LocalDate to);

        @Query("SELECT COUNT(tc) FROM TransferCost tc " +
                        "WHERE (:branchId IS NULL OR tc.transferRequest.sourceBranch.id = :branchId " +
                        "OR tc.transferRequest.destinationBranch.id = :branchId) " +
                        "AND (:from IS NULL OR CAST(tc.recordedAt AS date) >= :from) " +
                        "AND (:to IS NULL OR CAST(tc.recordedAt AS date) <= :to)")
        long countByFilters(@Param("branchId") Long branchId,
                        @Param("from") LocalDate from,
                        @Param("to") LocalDate to);
}