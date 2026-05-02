ALTER TABLE employee
    ADD COLUMN created_by VARCHAR(255) DEFAULT NULL,
    ADD COLUMN last_modified_by VARCHAR(255) DEFAULT NULL;

CREATE TABLE IF NOT EXISTS audit_log (
    id          BIGINT NOT NULL AUTO_INCREMENT,
    entity_name VARCHAR(100) NOT NULL,
    entity_id   BIGINT NOT NULL,
    action      VARCHAR(20) NOT NULL,
    field_name  VARCHAR(100) DEFAULT NULL,
    old_value   TEXT DEFAULT NULL,
    new_value   TEXT DEFAULT NULL,
    changed_by  VARCHAR(255) NOT NULL,
    changed_at  DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX IX_AUDIT_LOG_I (entity_name, entity_id),
    INDEX IX_AUDIT_LOG_II (changed_by)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;