package com.branch.inventory.backend.repository;

import com.branch.inventory.backend.model.StockLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockLevelRepository extends JpaRepository<StockLevel, Long> {

    Optional<StockLevel> findByBranchIdAndItemId(Long branchId, Long itemId);

    List<StockLevel> findByBranchId(Long branchId);

    List<StockLevel> findByItemId(Long itemId);

    boolean existsByBranchIdAndItemId(Long branchId, Long itemId);

    @Query("SELECT s FROM StockLevel s WHERE s.quantityOnHand <= s.minimumThreshold")
    List<StockLevel> findAllLowStock();

    @Query("SELECT s FROM StockLevel s WHERE s.branch.id = :branchId AND s.quantityOnHand <= s.minimumThreshold")
    List<StockLevel> findLowStockByBranch(@Param("branchId") Long branchId);

    @Query("SELECT s FROM StockLevel s WHERE " +
            "(:branchId IS NULL OR s.branch.id = :branchId) " +
            "AND (:itemId IS NULL OR s.item.id = :itemId) " +
            "AND (:category IS NULL OR s.item.category = :category)")
    Page<StockLevel> findByFilters(
            @Param("branchId") Long branchId,
            @Param("itemId") Long itemId,
            @Param("category") String category,
            Pageable pageable);

    @Query("SELECT s FROM StockLevel s WHERE " +
            "(:branchId IS NULL OR s.branch.id = :branchId) " +
            "AND (:itemId IS NULL OR s.item.id = :itemId) " +
            "AND (:category IS NULL OR s.item.category = :category)")
    List<StockLevel> findByFiltersForReport(
            @Param("branchId") Long branchId,
            @Param("itemId") Long itemId,
            @Param("category") String category);

    @Query("SELECT s FROM StockLevel s WHERE " +
            "s.branch.id = :branchId " +
            "AND s.quantityOnHand <= s.minimumThreshold " +
            "AND (:category IS NULL OR s.item.category = :category)")
    List<StockLevel> findLowStockByBranchAndCategory(
            @Param("branchId") Long branchId,
            @Param("category") String category);

    @Query("SELECT s FROM StockLevel s WHERE " +
            "s.quantityOnHand <= s.minimumThreshold " +
            "AND (:category IS NULL OR s.item.category = :category)")
    List<StockLevel> findAllLowStockByCategory(@Param("category") String category);

    @Query("SELECT COUNT(DISTINCT s.item.id) FROM StockLevel s WHERE s.branch.id = :branchId")
    long countDistinctItemsByBranch(@Param("branchId") Long branchId);

    @Query("SELECT COUNT(s) FROM StockLevel s WHERE s.branch.id = :branchId AND s.quantityOnHand <= s.minimumThreshold")
    long countLowStockByBranch(@Param("branchId") Long branchId);
}