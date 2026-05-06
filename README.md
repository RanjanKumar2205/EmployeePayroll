# Employee Payroll System

A production-grade REST API built with **Java 21 + Spring Boot 4.0**. Beyond standard CRUD, it implements stateless JWT authentication with method-level access control, an atomic salary revision flow that preserves full history via `@Transactional`, and a field-level audit log powered by reflection-based diffing. The project ships with a full test suite — unit tests with Mockito, `@SpringBootTest` integration tests, a dedicated security test class, and a Testcontainers variant that runs against a real MySQL instance.

> **Live API docs:** `http://localhost:8080/swagger-ui.html` (Swagger / OpenAPI 3 — JWT auth built into the UI)

---

## Key Design Decisions

| Area | Decision |
|---|---|
| Security | Stateless JWT (`OncePerRequestFilter`), method-level `@PreAuthorize`, ownership checks via SpEL |
| Data integrity | `@Transactional` salary revision — deactivates old record, creates new, rolls back atomically on failure |
| Audit trail | Field-level diffing via reflection — every `UPDATE`, `CREATE`, `DELETE` persisted to `audit_log` with old/new values |
| Layered design | Controllers see DTOs only; entities never leave the service layer; FK resolution in service, not mapper |
| JPA | `FetchType.LAZY` on all `@ManyToOne`; self-referencing manager hierarchy; `@EntityGraph`-ready repositories |
| Dynamic querying | `JpaSpecificationExecutor` + composable predicates — any filter combination without raw SQL |
| Testing | Unit (Mockito), integration (`@SpringBootTest` + MockMvc), security boundary tests, Testcontainers |
| Production config | Dev/prod profile separation, env-var–driven prod config, protected bootstrap admin account |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0 |
| Database | MySQL 8 |
| ORM | Spring Data JPA / Hibernate |
| Security | Spring Security + JWT (jjwt) |
| Build Tool | Gradle |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5, Mockito, MockMvc, Testcontainers |
| Utilities | Lombok, Jakarta Validation |

---

## Architecture

```
Request
   ↓
JwtAuthFilter        ← validates Bearer token, sets SecurityContext
   ↓
SecurityConfig       ← permit /auth/** and /health, authenticate everything else
   ↓
Controller           ← @PreAuthorize checks, validates input, delegates to service
   ↓
Service              ← business logic, resolves relationships, throws domain exceptions
   ↓                    writes to AuditLogService on mutating operations
Repository           ← DB access only (JpaRepository + JpaSpecificationExecutor)
   ↓
MySQL Database
   ↑
Entity               ← DB representation, never leaves service layer
Mapper               ← converts Entity ↔ DTO
DTO                  ← what crosses layer boundaries (Request in, Response out)
```

**Key design decisions:**
- Entity classes never leave the service layer — controllers only ever see DTOs
- All FK resolution happens in the service, not the mapper
- `FetchType.LAZY` on all `@ManyToOne` associations — no accidental N+1 eager loading
- `ResourceNotFoundException` thrown from service, caught by `GlobalExceptionHandler` — controllers stay clean
- `hasAuthority()` in all `@PreAuthorize` expressions — roles stored without `ROLE_` prefix
- `isProtected` flag on seeded admin — cannot be modified via API by anyone, ever

---

## Project Structure

```
src/main/java/org/example/employeepayroll/
├── config/
│   ├── SecurityConfig.java         # Filter chain, BCrypt bean, AuthManager
│   ├── DataSeeder.java             # Seeds protected admin on first startup
│   ├── OpenApiConfig.java          # Swagger UI with global JWT SecurityScheme
│   └── AuditorAwareImpl.java       # Spring Data Auditing — resolves current username
├── controllers/
│   ├── AuthController.java         # /auth/register, /auth/login
│   ├── EmployeeController.java
│   ├── DepartmentController.java
│   ├── SalaryController.java       # salary CRUD + atomic revision endpoint
│   ├── UserController.java         # /users/{id}/role
│   └── HealthController.java
├── services/
│   ├── AuthService.java            # register + login, auto user-employee linking
│   ├── EmployeeService.java        # CRUD + audit log calls
│   ├── DepartmentService.java
│   ├── SalaryService.java          # addSalary + @Transactional reviseSalary
│   ├── AuditLogService.java        # reflection-based field diff, persists every change
│   ├── UserService.java            # role update with isProtected checks
│   └── AuthorizationService.java  # isOwner() for @PreAuthorize SpEL
├── repositories/
│   ├── EmployeeRepository.java     # JpaSpecificationExecutor for dynamic search
│   ├── DepartmentRepository.java
│   ├── SalaryRepository.java       # findByEmployeeIdAndIsActiveTrue
│   ├── AuditLogRepository.java
│   └── UserRepository.java
├── entities/
│   ├── Employee.java               # self-referencing manager FK
│   ├── Department.java
│   ├── Users.java
│   ├── SalaryStructure.java        # effectiveFrom/To dates, isActive flag
│   ├── AuditLog.java               # entityName, entityId, field, oldValue, newValue
│   ├── Role.java                   # ADMIN, HR, EMPLOYEE, GUEST
│   ├── EmployeeType.java
│   └── Status.java
├── dtos/                           # Request/Response DTOs for every domain
├── mappers/                        # Entity ↔ DTO; AuthMapper handles BCrypt encoding
├── specifications/
│   └── EmployeeSpecification.java  # Composable JPA predicates for dynamic search
├── exceptions/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── DuplicateResourceException.java
└── validators/
    ├── ContactNumber.java           # Custom constraint annotation
    └── ContactNumberValidator.java  # 10-digit phone validation
```

```
src/test/java/org/example/employeepayroll/
├── services/
│   └── EmployeeServiceTest.java             # Unit tests with Mockito
├── EmployeeControllerIntegrationTest.java   # @SpringBootTest + MockMvc + @WithMockUser
├── EmployeeControllerTCIntegrationTest.java # Testcontainers variant (real MySQL)
├── SecurityTest.java                        # 401/403 boundary tests
└── utils/
    └── JwtUtilTest.java                     # Token generation and validation
```

---

## Authentication & Authorization

### Flow

```
POST /auth/register  →  User created (GUEST by default)
                        If username matches Employee.email → auto-linked + promoted to EMPLOYEE
POST /auth/login     →  Returns signed JWT
All other endpoints  →  Require Authorization: Bearer <token>
```

### Roles

| Role | Assigned by | Access |
|---|---|---|
| `GUEST` | Default on register | No employee data access |
| `EMPLOYEE` | Auto on register if email matches employee record | Own record only |
| `HR` | ADMIN via `PATCH /users/{id}/role` | All employee data, no delete |
| `ADMIN` | Seeded on startup or promoted by existing ADMIN | Full access |

### Endpoint Access Matrix

| Endpoint | ADMIN | HR | EMPLOYEE | GUEST |
|---|---|---|---|---|
| `GET /employees/` | ✓ | ✓ | ✗ | ✗ |
| `GET /employees/{id}` | ✓ | ✓ | own only | ✗ |
| `GET /employees/search` | ✓ | ✓ | ✓ | ✗ |
| `POST /employees/` | ✓ | ✓ | ✗ | ✗ |
| `PUT /employees/{id}` | ✓ | ✓ | ✗ | ✗ |
| `PATCH /employees/{id}` | ✓ | ✓ | ✗ | ✗ |
| `DELETE /employees/{id}` | ✓ | ✗ | ✗ | ✗ |
| `GET /salaryStructure/` | ✓ | ✓ | own only | ✗ |
| `POST /salaryStructure/` | ✓ | ✓ | ✗ | ✗ |
| `POST /salaryStructure/revise/{id}` | ✓ | ✓ | ✗ | ✗ |
| `PATCH /users/{id}/role` | ✓ | ✗ | ✗ | ✗ |

**Admin protection rules:**
- An admin cannot modify their own role
- The seeded admin account is protected — no API call can modify it regardless of caller. Changes require direct DB access, which is intentional

---

## Feature Deep-Dives

### Transactional Salary Revision

Salary history is preserved as an immutable ledger. The `reviseSalary` endpoint deactivates the current record (`isActive = false`, sets `effectiveTo`) and inserts a new active record — both within a single `@Transactional` boundary. If anything fails, the entire operation rolls back, leaving no orphaned salary records.

```
POST /api/v1/salaryStructure/revise/{employeeId}

Before: SalaryStructure(id=1, isActive=true,  effectiveTo=null)
After:  SalaryStructure(id=1, isActive=false, effectiveTo=today)
        SalaryStructure(id=2, isActive=true,  effectiveFrom=today)
```

### Field-Level Audit Logging

`AuditLogService` compares entity snapshots using reflection, skipping JPA relationship fields (`@ManyToOne`, `@OneToMany`, etc.) to avoid proxy issues. Every changed field is persisted as its own row with `oldValue` and `newValue`. FK changes are tracked separately via `logFkChange()`. The result is a queryable history of every mutation on every entity.

```json
// GET /api/v1/employees/{id}/audit  (example record)
{
  "entityName": "Employee",
  "entityId": 42,
  "action": "UPDATE",
  "fieldName": "designation",
  "oldValue": "Software Engineer",
  "newValue": "Senior Software Engineer",
  "changedBy": "admin@company.com",
  "changedAt": "2026-04-10T14:32:00"
}
```

### Dynamic Search with JPA Specifications

Each filter is an independent, composable predicate. Missing parameters are simply not included in the `WHERE` clause — no string concatenation, no raw SQL.

```
GET /api/v1/employees/search?name=ranjan&dept=Engineering&status=ACTIVE&page=0&size=10&sort=lastName,asc
```

### Pagination

All list endpoints support Spring's `Pageable` auto-binding:

```
GET /api/v1/employees/?page=0&size=10&sort=lastName,asc
```

```json
{
  "content": [...],
  "totalElements": 47,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

### Consistent Error Responses

All errors — validation failures, missing resources, auth failures, unexpected exceptions — return the same JSON envelope from `GlobalExceptionHandler`:

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
| Duplicate resource | 409 |
| Validation failure | 400 with field-level errors |
| Bad credentials | 401 |
| Unauthorized access | 403 |
| Unexpected error | 500 |

---

## Data Model

```
Users
  id            BIGINT PK
  username      VARCHAR UNIQUE (email)
  password      VARCHAR (BCrypt)
  role          ENUM(ADMIN, HR, EMPLOYEE, GUEST)
  isProtected   BOOLEAN
  employee_id   FK → Employee.id  (nullable)

Department
  id, name, code, status ENUM(ACTIVE, INACTIVE, HOLD, DELETE)

Employee
  id, employeeCode, firstName, lastName, email UNIQUE
  phoneNumber, designation, dateOfJoining
  employeeType  ENUM(FULL_TIME, PART_TIME, CONTRACT, INTERN)
  status        ENUM(ACTIVE, INACTIVE, HOLD, DELETE)
  department_id FK → Department.id
  manager_id    FK → Employee.id  (self-referencing hierarchy)
  createdAt, updatedAt  (auto-managed)

SalaryStructure
  id, employee_id FK, basicSalary, hra, specialAllowance
  pfEmployee, pfEmployer, professionalTax, tds
  effectiveFrom, effectiveTo, isActive

AuditLog
  id, entityName, entityId, action ENUM(CREATE, UPDATE, DELETE)
  fieldName, oldValue, newValue, changedBy, changedAt
```

**JPA relationships:**
- `Users → Employee`: `@OneToOne` — one system account per employee
- `Employee → Department`: `@ManyToOne(LAZY)` — many employees per department
- `Employee → Employee`: self-referencing `@ManyToOne(LAZY)` for manager hierarchy
- `Employee → SalaryStructure`: `@OneToMany` — full salary history per employee

---

## Running Locally

### Prerequisites
- Java 21
- MySQL 8
- Gradle

### Setup

```bash
# 1. Clone
git clone https://github.com/RanjanKumar2205/EmployeePayroll.git
cd EmployeePayroll

# 2. Create DB
mysql -u root -p -e "CREATE DATABASE employee_payroll;"

# 3. Configure
cp src/main/resources/application-dev.properties.example \
   src/main/resources/application-dev.properties
```

Edit `application-dev.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/employee_payroll
spring.datasource.username=root
spring.datasource.password=your_password

jwt.secret=your-secret-key-must-be-at-least-32-characters
jwt.expiry=3600

app.admin.username=admin@yourcompany.com
app.admin.password=your_admin_password
```

```bash
# 4. Run
./gradlew bootRun
```

App starts on `http://localhost:8080`. Verify with:
```bash
curl http://localhost:8080/api/v1/health   # → UP
```

The seeded admin is created automatically on first startup if no admin exists.

Open `http://localhost:8080/swagger-ui.html` to explore the API interactively.

---

## Running Tests

```bash
# Unit + integration tests (uses in-memory H2)
./gradlew test

# Testcontainers integration tests (requires Docker — spins up real MySQL)
./gradlew test -Dspring.profiles.active=testcontainers
```

---

## Environment Profiles

| Profile | Purpose | Activated by |
|---|---|---|
| `dev` | Local MySQL | Default |
| `prod` | Reads from env vars | `SPRING_PROFILES_ACTIVE=prod` |
| `test` | In-memory H2 | `@ActiveProfiles("test")` in tests |
| `testcontainers` | Real MySQL via Docker | Testcontainers suite |

Production env vars:
```
DB_URL        DB_USER        DB_PASSWORD
JWT_SECRET    JWT_EXPIRY
ADMIN_USERNAME  ADMIN_PASSWORD
```

---

## API Reference

### Auth

| Method | Endpoint | Auth |
|---|---|---|
| POST | `/api/v1/auth/register` | None |
| POST | `/api/v1/auth/login` | None |

### Employees

| Method | Endpoint | Auth |
|---|---|---|
| GET | `/api/v1/employees/` | ADMIN, HR |
| GET | `/api/v1/employees/{id}` | ADMIN, HR, owner |
| GET | `/api/v1/employees/search` | ADMIN, HR, EMPLOYEE |
| POST | `/api/v1/employees/` | ADMIN, HR |
| PUT | `/api/v1/employees/{id}` | ADMIN, HR |
| PATCH | `/api/v1/employees/{id}` | ADMIN, HR |
| DELETE | `/api/v1/employees/{id}` | ADMIN |

### Salary

| Method | Endpoint | Auth |
|---|---|---|
| GET | `/api/v1/salaryStructure/` | ADMIN, HR, owner |
| GET | `/api/v1/salaryStructure/{id}` | ADMIN, HR |
| POST | `/api/v1/salaryStructure/` | ADMIN, HR |
| POST | `/api/v1/salaryStructure/revise/{employeeId}` | ADMIN, HR |

### Departments

| Method | Endpoint | Auth |
|---|---|---|
| GET | `/api/v1/departments/` | Authenticated |
| GET | `/api/v1/departments/{id}` | Authenticated |
| POST | `/api/v1/departments/` | ADMIN, HR |
| PUT | `/api/v1/departments/{id}` | ADMIN, HR |
| PATCH | `/api/v1/departments/{id}` | ADMIN, HR |
| DELETE | `/api/v1/departments/{id}` | ADMIN |

### Users & Health

| Method | Endpoint | Auth |
|---|---|---|
| PATCH | `/api/v1/users/{id}/role` | ADMIN |
| GET | `/api/v1/health` | None |

---

## Sample Requests

```bash
# Register (auto-linked as EMPLOYEE if email matches an employee record)
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "ranjan@example.com", "password": "password123"}'

# Login → save the token
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin@yourcompany.com", "password": "admin_pass"}' | jq -r .token)

# Create an employee
curl -X POST http://localhost:8080/api/v1/employees/ \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeCode": "EMP001", "firstName": "Ranjan", "lastName": "Kumar",
    "email": "ranjan@example.com", "phoneNumber": "9876543210",
    "designation": "Software Engineer", "dateOfJoining": "2026-03-21",
    "employeeType": "FULL_TIME", "departmentId": 1
  }'

# Assign a salary
curl -X POST http://localhost:8080/api/v1/salaryStructure/ \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": 1, "basicSalary": 50000, "hra": 20000,
    "specialAllowance": 10000, "pfEmployee": 1800, "pfEmployer": 1800,
    "professionalTax": 200, "tds": 5000, "effectiveFrom": "2026-04-01"
  }'

# Revise salary (atomic — old record deactivated, new record created in one transaction)
curl -X POST http://localhost:8080/api/v1/salaryStructure/revise/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"employeeId": 1, "basicSalary": 60000, "hra": 24000, ...}'

# Dynamic search
curl "http://localhost:8080/api/v1/employees/search?name=ranjan&status=ACTIVE&page=0&size=5" \
  -H "Authorization: Bearer $TOKEN"

# Promote a user to HR
curl -X PATCH http://localhost:8080/api/v1/users/2/role \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"role": "HR"}'
```

---

## Roadmap

- [ ] N+1 fix — `@EntityGraph` / `JOIN FETCH` on list queries
- [ ] Redis caching — `@Cacheable` on frequently read data (config scaffolded, pending integration)
- [ ] Flyway — versioned SQL migrations replacing `ddl-auto=update`
- [ ] Docker + docker-compose — containerised local setup
- [ ] AWS deployment — EC2 + RDS + GitHub Actions CI/CD pipeline