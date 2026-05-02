-- Department (no FK yet)
CREATE TABLE IF NOT EXISTS department (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(255) DEFAULT NULL,
    name VARCHAR(255) DEFAULT NULL,
    status ENUM('ACTIVE','DELETE','HOLD','INACTIVE') NOT NULL,
    manager_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    KEY IX_DEPARTMENT_I (manager_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Employee
CREATE TABLE IF NOT EXISTS employee (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    date_of_joining DATE DEFAULT NULL,
    designation VARCHAR(255) DEFAULT NULL,
    email VARCHAR(255) DEFAULT NULL,
    employee_code VARCHAR(255) DEFAULT NULL,
    employee_type ENUM('CONTRACT','FULL_TIME','PART_TIME','INTERN') DEFAULT NULL,
    first_name VARCHAR(255) DEFAULT NULL,
    last_name VARCHAR(255) DEFAULT NULL,
    phone_number VARCHAR(255) DEFAULT NULL,
    status ENUM('ACTIVE','INACTIVE','HOLD','DELETE') DEFAULT NULL,
    updated_at DATETIME(6) DEFAULT NULL,
    department_id BIGINT DEFAULT NULL,
    manager_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    KEY IX_EMPLOYEE_II (manager_id),
    KEY IX_EMPLOYEE_I (department_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- User
CREATE TABLE IF NOT EXISTS user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    password VARCHAR(255) DEFAULT NULL,
    username VARCHAR(255) DEFAULT NULL,
    employee_id BIGINT DEFAULT NULL,
    role ENUM('ADMIN','EMPLOYEE','GUEST','HR') DEFAULT NULL,
    is_protected BIT(1) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UK_USER_EMPLOYEE (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Salary Structure
CREATE TABLE IF NOT EXISTS salary_structure (
    id BIGINT NOT NULL AUTO_INCREMENT,
    basic_salary DOUBLE DEFAULT NULL,
    effective_from DATE DEFAULT NULL,
    effective_to DATE DEFAULT NULL,
    hra DOUBLE DEFAULT NULL,
    is_active BIT(1) NOT NULL,
    pf_employee DOUBLE DEFAULT NULL,
    pf_employer DOUBLE DEFAULT NULL,
    professional_tax DOUBLE DEFAULT NULL,
    special_allowance DOUBLE DEFAULT NULL,
    tds DOUBLE DEFAULT NULL,
    employee_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY IX_SALARY_STRUCTURE_I (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0