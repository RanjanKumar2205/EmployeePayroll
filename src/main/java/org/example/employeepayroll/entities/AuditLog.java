package org.example.employeepayroll.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="audit_log", indexes = {
        @Index(name = "IX_AUDIT_LOG_I", columnList = "entity_name, entity_id"),
        @Index(name = "IX_AUDIT_LOG_II", columnList = "changed_by")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entityName;

    @Column(nullable = false)
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    private String fieldName;

    @Column(columnDefinition = "TEXT")
    private String oldValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    @Column(nullable = false)
    private String changedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime changedAt;
}