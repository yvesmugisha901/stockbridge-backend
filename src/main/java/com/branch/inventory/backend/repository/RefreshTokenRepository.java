package com.branch.inventory.backend.repository;

import com.branch.inventory.backend.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Eagerly fetches the user so callers can access user fields without a session.
     */
    @Query("SELECT r FROM RefreshToken r JOIN FETCH r.user u LEFT JOIN FETCH u.branch WHERE r.token = :token")
    Optional<RefreshToken> findByToken(@Param("token") String token);

    /** Revoke by user ID — avoids passing a lazy proxy as a parameter. */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user.id = :userId")
    void revokeAllByUserId(@Param("userId") Long userId);
}
