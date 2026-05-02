package org.example.employeepayroll.repositories;

import org.example.employeepayroll.entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityNameAndEntityIdOrderByChangedAtDesc(
            String entityName, Long entityId);
}