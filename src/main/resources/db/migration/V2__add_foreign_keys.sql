-- Employee FKs
ALTER TABLE employee
    ADD CONSTRAINT FK_EMPLOYEE_DEPARTMENT FOREIGN KEY (department_id) REFERENCES department (id),
    ADD CONSTRAINT FK_EMPLOYEE_MANAGER FOREIGN KEY (manager_id) REFERENCES employee (id);

-- Department FK (circular, added after employee exists)
ALTER TABLE department
    ADD CONSTRAINT FK_DEPARTMENT_MANAGER FOREIGN KEY (manager_id) REFERENCES employee (id);

-- User FK
ALTER TABLE user
    ADD CONSTRAINT FK_USER_EMPLOYEE FOREIGN KEY (employee_id) REFERENCES employee (id);

-- Salary Structure FK
ALTER TABLE salary_structure
    ADD CONSTRAINT FK_SALARY_EMPLOYEE FOREIGN KEY (employee_id) REFERENCES employee (id);
