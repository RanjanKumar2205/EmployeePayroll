package org.example.employeepayroll.services;

import org.example.employeepayroll.config.AuditorAwareImpl;
import org.example.employeepayroll.entities.AuditAction;
import org.example.employeepayroll.entities.AuditLog;
import org.example.employeepayroll.repositories.AuditLogRepository;
import org.springframework.stereotype.Service;

import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditorAwareImpl auditorAware;

    public AuditLogService(AuditLogRepository auditLogRepository, AuditorAwareImpl auditorAware) {
        this.auditLogRepository = auditLogRepository;
        this.auditorAware = auditorAware;
    }

    public void logCreate(String entityName, Long entityId) {
        save(entityName, entityId, AuditAction.CREATE, null, null, null);
    }

    public void logDelete(String entityName, Long entityId) {
        save(entityName, entityId, AuditAction.DELETE, null, null, null);
    }

    public void logChanges(String entityName, Long entityId,
                           Object oldSnapshot, Object current) {
        diff(oldSnapshot, current).forEach((field, values) ->
                save(entityName, entityId, AuditAction.UPDATE,
                        field, values[0], values[1])
        );
    }

    public List<AuditLog> getHistory(String entityName, Long entityId) {
        return auditLogRepository
                .findByEntityNameAndEntityIdOrderByChangedAtDesc(entityName, entityId);
    }

    private Map<String, String[]> diff(Object oldObj, Object newObj) {
        Map<String, String[]> changes = new LinkedHashMap<>();
        Class<?> clazz = oldObj.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (isRelationshipField(field) || field.isSynthetic()) continue;

            field.setAccessible(true);
            try {
                String oldVal = stringify(field.get(oldObj));
                String newVal = stringify(field.get(newObj));
                if (!Objects.equals(oldVal, newVal)) {
                    changes.put(field.getName(), new String[]{oldVal, newVal});
                }
            } catch (IllegalAccessException e) {
                // skip inaccessible fields
            }
        }
        return changes;
    }

    private boolean isRelationshipField(Field field) {
        return field.isAnnotationPresent(ManyToOne.class)
                || field.isAnnotationPresent(OneToMany.class)
                || field.isAnnotationPresent(OneToOne.class)
                || field.isAnnotationPresent(ManyToMany.class);
    }

    private String stringify(Object value) {
        return value != null ? value.toString() : null;
    }

    private void save(String entityName, Long entityId, AuditAction action,
                      String fieldName, String oldValue, String newValue) {
        auditLogRepository.save(
                AuditLog.builder()
                        .entityName(entityName)
                        .entityId(entityId)
                        .action(action)
                        .fieldName(fieldName)
                        .oldValue(oldValue)
                        .newValue(newValue)
                        .changedBy(auditorAware.getCurrentAuditor()
                                .orElse("system"))
                        .changedAt(LocalDateTime.now())
                        .build()
        );
    }

    // In AuditLogService — add this alongside logChanges
    public void logFkChange(String entityName, Long entityId,
                            String fieldName, Long oldId, Long newId) {
        if (!Objects.equals(oldId, newId)) {
            save(entityName, entityId, AuditAction.UPDATE,
                    fieldName,
                    oldId != null ? oldId.toString() : null,
                    newId != null ? newId.toString() : null);
        }
    }
}