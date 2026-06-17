package com.branch.inventory.backend.repository;

import com.branch.inventory.backend.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByPerformedBy(String performedBy);

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<AuditLog> findByAction(String action);

    List<AuditLog> findByEntityTypeAndEntityIdOrderByPerformedAtDesc(String entityType, Long entityId);

    @Query("SELECT a FROM AuditLog a WHERE a.performedAt BETWEEN :start AND :end ORDER BY a.performedAt DESC")
    List<AuditLog> findByPerformedAtBetween(@Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    List<AuditLog> findTop50ByOrderByPerformedAtDesc();

    // ── New: used by the admin audit log page with filters + pagination ──
    @Query("""
            SELECT a FROM AuditLog a
            WHERE
              (:search IS NULL OR :search = '' OR
                LOWER(a.performedBy) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(a.action)      LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(a.details)     LIKE LOWER(CONCAT('%', :search, '%'))
              )
              AND (:action     IS NULL OR :action     = '' OR a.action               = :action)
              AND (:entityType IS NULL OR :entityType = '' OR LOWER(a.entityType)    = LOWER(:entityType))
              AND (:from       IS NULL OR a.performedAt >= :from)
              AND (:to         IS NULL OR a.performedAt <= :to)
            """)
    Page<AuditLog> findFiltered(
            @Param("search") String search,
            @Param("action") String action,
            @Param("entityType") String entityType,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);
}
