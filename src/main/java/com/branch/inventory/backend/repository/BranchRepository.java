package com.branch.inventory.backend.repository;

import com.branch.inventory.backend.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    Optional<Branch> findByName(String name);

    Optional<Branch> findByCode(String code);

    boolean existsByName(String name);

    boolean existsByCode(String code);

    List<Branch> findByActiveTrue(); // fixed: was findByIsActiveTrue()

    List<Branch> findByActiveFalse(); // fixed: was findByIsActiveFalse()
}