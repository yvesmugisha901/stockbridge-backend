package com.branch.inventory.backend.repository;

import com.branch.inventory.backend.model.User;
import com.branch.inventory.backend.model.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.branch WHERE u.email = :email")
    Optional<User> findByEmailWithBranch(@Param("email") String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.branch WHERE u.id = :id")
    Optional<User> findByIdWithBranch(@Param("id") Long id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.branch")
    Page<User> findAllWithBranch(Pageable pageable);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    List<User> findByBranchId(Long branchId);

    List<User> findByActiveTrue();

    List<User> findByActiveFalse();

    List<User> findByRoleAndBranchId(Role role, Long branchId);

    List<User> findByBranchIdAndRole(Long branchId, Role role);

    // Used by AdminService.getStats() — counts users created this calendar month
    long countByCreatedAtAfter(LocalDateTime date);
}
