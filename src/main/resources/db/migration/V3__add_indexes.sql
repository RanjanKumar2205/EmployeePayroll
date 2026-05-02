-- Employee indexes
CREATE INDEX IX_EMPLOYEE_I
    ON employee (department_id, status);

CREATE INDEX IX_EMPLOYEE_II
    ON employee (manager_id);

CREATE INDEX IX_EMPLOYEE_III
    ON employee (email);

CREATE INDEX IX_EMPLOYEE_IV
    ON employee (status);

-- Department index
CREATE INDEX IX_DEPARTMENT_I
    ON department (manager_id);

-- User unique index
CREATE UNIQUE INDEX UK_USER_EMPLOYEE
    ON user (employee_id);

-- Salary Structure indexes
CREATE INDEX IX_SALARY_STRUCTURE_I
    ON salary_structure (employee_id);

CREATE INDEX IX_SALARY_STRUCTURE_II
    ON salary_structure (employee_id, is_active);

CREATE INDEX IX_SALARY_STRUCTURE_III
    ON salary_structure (effective_from, effective_to);
