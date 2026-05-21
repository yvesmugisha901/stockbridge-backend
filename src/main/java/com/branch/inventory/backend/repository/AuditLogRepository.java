package com.branch.inventory.backend.repository;

import com.branch.inventory.backend.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByPerformedBy(String performedBy); // fixed: was findByPerformedById(Long)

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<AuditLog> findByAction(String action);

    // fixed: was OrderByCreatedAtDesc — field is performedAt
    List<AuditLog> findByEntityTypeAndEntityIdOrderByPerformedAtDesc(String entityType, Long entityId);

    // fixed: field is performedAt not createdAt
    @Query("SELECT a FROM AuditLog a WHERE a.performedAt BETWEEN :start AND :end ORDER BY a.performedAt DESC")
    List<AuditLog> findByPerformedAtBetween(@Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // fixed: field is performedAt not createdAt
    List<AuditLog> findTop50ByOrderByPerformedAtDesc();
}