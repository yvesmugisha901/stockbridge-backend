package com.branch.inventory.backend.repository;

import com.branch.inventory.backend.model.User;
import com.branch.inventory.backend.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email); // used by AuthService, UserDetailsService

    boolean existsByEmail(String email); // used by UserService.createUser()

    List<User> findByRole(Role role);

    List<User> findByBranchId(Long branchId);

    List<User> findByActiveTrue(); // fixed from findByIsActiveTrue()

    List<User> findByActiveFalse(); // fixed from findByIsActiveFalse()

    List<User> findByRoleAndBranchId(Role role, Long branchId);
}