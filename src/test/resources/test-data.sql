-- Seed a department (id=100 to avoid collision with DataSeeder rows)
INSERT INTO department (id, name, code, status) VALUES (100, 'Engineering', 'ENG', 'ACTIVE');

-- Seed two employees linked to that department
-- created_at is NOT NULL on the column so we must supply it
INSERT INTO employee (id, employee_code, first_name, last_name, email, phone_number,
                      designation, employee_type, status, date_of_joining,
                      department_id, created_at)
VALUES (100, 'EMP100', 'Rahul', 'Sharma', 'rahul.sharma@test.com', '9876543210',
        'Software Engineer', 'FULL_TIME', 'ACTIVE', '2024-01-15',
        100, '2024-01-15 10:00:00');

INSERT INTO employee (id, employee_code, first_name, last_name, email, phone_number,
                      designation, employee_type, status, date_of_joining,
                      department_id, created_at)
VALUES (101, 'EMP101', 'Priya', 'Singh', 'priya.singh@test.com', '9876543211',
        'Senior Engineer', 'FULL_TIME', 'ACTIVE', '2023-06-01',
        100, '2023-06-01 09:00:00');