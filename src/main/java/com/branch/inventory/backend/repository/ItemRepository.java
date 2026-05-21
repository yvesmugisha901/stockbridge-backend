package com.branch.inventory.backend.repository;

import com.branch.inventory.backend.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findByCode(String code);

    boolean existsByCode(String code);

    List<Item> findByCategory(String category);

    List<Item> findByActiveTrue();

    Page<Item> findByActiveTrue(Pageable pageable);

    Page<Item> findByCategoryAndActiveTrue(String category, Pageable pageable);

    Page<Item> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    Page<Item> findByNameContainingIgnoreCaseAndCategoryAndActiveTrue(String name, String category, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE " +
            "LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(i.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(i.category) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Item> searchByKeyword(@Param("keyword") String keyword);

    List<Item> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(String name, String code);
}