# Employee Payroll System

A production-grade REST API built with Spring Boot, demonstrating real-world backend engineering patterns — layered architecture, JPA relationships, exception handling, dynamic search with JPA Specifications, pagination, and environment-based configuration.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0 |
| Database | MySQL 8 |
| ORM | Spring Data JPA / Hibernate |
| Build Tool | Gradle |
| Utilities | Lombok, Jakarta Validation |

---

## Architecture

```
Request
   ↓
Controller       ← validates input, delegates to service, returns ResponseEntity
   ↓
Service          ← business logic, resolves relationships, throws exceptions
   ↓
Repository       ← DB access only (JpaRepository + JpaSpecificationExecutor)
   ↓
MySQL Database
   ↑
Entity           ← DB representation, never leaves service layer
Mapper           ← converts Entity ↔ DTO
DTO              ← what crosses layer boundaries (Request in, Response out)
```

**Key design decisions:**
- Entity classes never leave the service layer — controllers only see DTOs
- All relationship resolution (FK lookups) happens in the service, not the mapper
- `FetchType.LAZY` on all `@ManyToOne` associations — no accidental eager loading
- `ResourceNotFoundException` thrown from service, caught by `GlobalExceptionHandler` — controllers stay clean

---

## Project Structure

```
src/main/java/org/eample/employeepayroll/
├── controllers/          # REST endpoints
│   ├── EmployeeController.java
│   ├── DepartmentController.java
│   └── HealthController.java
├── services/             # Business logic
│   ├── EmployeeService.java
│   └── DepartmentService.java
├── repositories/         # DB access
│   ├── EmployeeRepository.java
│   └── DepartmentRepository.java
├── entities/             # JPA entities
│   ├── Employee.java
│   ├── Department.java
│   ├── EmployeeType.java
│   └── Status.java
├── dtos/                 # Data Transfer Objects
│   ├── EmployeeRequestDto.java
│   ├── EmployeeResponseDto.java
│   ├── DepartmentRequestDto.java
│   ├── DepartmentResponseDto.java
│   ├── EmployeeSummaryDto.java
│   └── ErrorResponseDto.java
├── mappers/              # Entity ↔ DTO conversion
│   ├── EmployeeMapper.java
│   └── DepartmentMapper.java
├── specifications/       # Dynamic query filters
│   └── EmployeeSpecification.java
├── exceptions/           # Exception handling
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
└── validators/           # Custom validation
    ├── ContactNumber.java
    └── ContactNumberValidator.java
```

---

## API Reference

### Health

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/v1/health` | Application health check | None |

### Employees

| Method | Endpoint | Description | Status Codes |
|---|---|---|---|
| GET | `/api/v1/employees/` | List all employees (paginated) | 200 |
| GET | `/api/v1/employees/{id}` | Get employee by ID | 200, 404 |
| GET | `/api/v1/employees/search` | Search with optional filters | 200 |
| POST | `/api/v1/employees/` | Create new employee | 201, 400, 409 |
| PUT | `/api/v1/employees/{id}` | Full update (all fields) | 200, 400, 404 |
| PATCH | `/api/v1/employees/{id}` | Partial update (provided fields only) | 200, 404 |
| DELETE | `/api/v1/employees/{id}` | Soft delete (sets status=DELETE) | 200, 404 |

### Departments

| Method | Endpoint | Description | Status Codes |
|---|---|---|---|
| GET | `/api/v1/departments/` | List all departments | 200 |
| GET | `/api/v1/departments/{id}` | Get department by ID | 200, 404 |
| POST | `/api/v1/departments/` | Create new department | 201, 400 |
| PUT | `/api/v1/departments/{id}` | Full update | 200, 400, 404 |
| PATCH | `/api/v1/departments/{id}` | Partial update | 200, 404 |
| DELETE | `/api/v1/departments/{id}` | Soft delete | 200, 404 |

---

## Key Features

### Pagination & Sorting
All list endpoints support pagination via query params — Spring auto-binds them into `Pageable`:

```
GET /api/v1/employees/?page=0&size=10&sort=lastName,asc
```

Response includes metadata alongside the data:
```json
{
  "content": [...],
  "totalElements": 47,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

### Dynamic Search
`GET /api/v1/employees/search` accepts any combination of optional filters — all conditions are AND:

```
GET /api/v1/employees/search?name=raj&dept=IT&status=ACTIVE&page=0&size=10
```

Implemented using JPA Specifications — each filter is an independent, composable predicate. Missing params are skipped, not included in the WHERE clause.

### Consistent Error Responses
All errors return the same JSON shape regardless of where they originate:

```json
{
  "timestamp": "2026-04-05T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Employee with id: 999 not found",
  "path": "/api/v1/employees/999"
}
```

| Scenario | Status |
|---|---|
| Resource not found | 404 |
| Validation failure | 400 with field-level errors |
| Unexpected error | 500 |

### Input Validation
`EmployeeRequestDto` validates all fields on POST and PUT:

```java
@NotBlank               → employeeCode, firstName, lastName, designation
@Email                  → email
@NotNull                → dateOfJoining, employeeType, departmentId
@ContactNumber          → phoneNumber (custom: 10 digits, numbers only)
```

---

## Data Model

```
Department
  id          BIGINT PK AUTO_INCREMENT
  name        VARCHAR
  code        VARCHAR
  status      ENUM(ACTIVE, INACTIVE, HOLD, DELETE)
  manager_id  FK → Employee.id

Employee
  id              BIGINT PK AUTO_INCREMENT
  employeeCode    VARCHAR
  firstName       VARCHAR
  lastName        VARCHAR
  email           VARCHAR
  phoneNumber     VARCHAR
  designation     VARCHAR
  dateOfJoining   DATE
  employeeType    ENUM(FULL_TIME, PART_TIME, CONTRACT, INTERN)
  status          ENUM(ACTIVE, INACTIVE, HOLD, DELETE)  DEFAULT ACTIVE
  department_id   FK → Department.id
  manager_id      FK → Employee.id  (self-referencing)
  createdAt       DATETIME  auto-set on insert
  updatedAt       DATETIME  auto-set on update
```

**Relationships:**
- Many employees belong to one department (`@ManyToOne`)
- One employee can have one manager, who is also an employee (self-referencing `@ManyToOne`)
- One department has one manager (`@ManyToOne` on Department → Employee)
- All `@ManyToOne` associations use `FetchType.LAZY`

---

## Running Locally

### Prerequisites
- Java 21
- MySQL 8 running locally
- Gradle

### Setup

**1. Clone the repo**
```bash
git clone https://github.com/RanjanKumar2205/EmployeePayroll.git
cd EmployeePayroll
```

**2. Create the database**
```sql
CREATE DATABASE employee_payroll;
```

**3. Configure credentials**

Copy the example config and fill in your local values:
```bash
cp src/main/resources/application-dev.properties.example \
   src/main/resources/application-dev.properties
```

Edit `application-dev.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/employee_payroll
spring.datasource.username=root
spring.datasource.password=your_password
```

**4. Run**
```bash
./gradlew bootRun
```

App starts on `http://localhost:8080`. Verify:
```bash
curl http://localhost:8080/api/v1/health
# → UP
```

---

## Environment Profiles

| Profile | Purpose | Activated by |
|---|---|---|
| `dev` | Local development with MySQL | Default (`spring.profiles.active=dev`) |
| `prod` | Production — reads DB credentials from env vars | `SPRING_PROFILES_ACTIVE=prod` |

Production expects these environment variables:
```
DB_URL        jdbc:mysql://<host>:3306/<db>
DB_USER       database username
DB_PASSWORD   database password
```

---

## Sample Requests

**Create a department**
```bash
curl -X POST http://localhost:8080/api/v1/departments/ \
  -H "Content-Type: application/json" \
  -d '{"name": "Engineering", "code": "ENG"}'
```

**Create an employee**
```bash
curl -X POST http://localhost:8080/api/v1/employees/ \
  -H "Content-Type: application/json" \
  -d '{
    "employeeCode": "EMP001",
    "firstName": "Ranjan",
    "lastName": "Kumar",
    "email": "ranjan@example.com",
    "phoneNumber": "9876543210",
    "designation": "Software Engineer",
    "dateOfJoining": "2026-03-21",
    "employeeType": "FULL_TIME",
    "departmentId": 1
  }'
```

**Search employees**
```bash
curl "http://localhost:8080/api/v1/employees/search?name=ranjan&status=ACTIVE&page=0&size=5"
```

**Partial update**
```bash
curl -X PATCH http://localhost:8080/api/v1/employees/1 \
  -H "Content-Type: application/json" \
  -d '{"designation": "Senior Software Engineer"}'
```

---

## What's Next

- [ ] Spring Security — JWT authentication and role-based access (ADMIN / HR / EMPLOYEE)
- [ ] `@Transactional` — salary revision logic with rollback on failure
- [ ] N+1 fix — `@EntityGraph` and `JOIN FETCH` for optimised queries
- [ ] Redis caching — `@Cacheable` on frequently read data
- [ ] Flyway — SQL migration files replacing `ddl-auto=update`
- [ ] Unit tests — JUnit 5 + Mockito for service layer
- [ ] Integration tests — `@SpringBootTest` with real DB
- [ ] Swagger / OpenAPI — auto-generated API documentation
- [ ] Docker + docker-compose — containerised local setup
- [ ] AWS deployment — EC2 + RDS + GitHub Actions CI/CD
