# Employee Payroll System

A production-grade REST API built with Spring Boot, demonstrating real-world backend engineering patterns — layered architecture, JPA relationships, JWT authentication, role-based access control, exception handling, dynamic search with JPA Specifications, pagination, and environment-based configuration.

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
| Utilities | Lombok, Jakarta Validation |

---

## Architecture

```
Request
   ↓
JwtAuthFilter     ← validates Bearer token, sets SecurityContext
   ↓
SecurityConfig    ← permit /auth/** and /health, authenticate everything else
   ↓
Controller        ← @PreAuthorize checks, validates input, delegates to service
   ↓
Service           ← business logic, resolves relationships, throws exceptions
   ↓
Repository        ← DB access only (JpaRepository + JpaSpecificationExecutor)
   ↓
MySQL Database
   ↑
Entity            ← DB representation, never leaves service layer
Mapper            ← converts Entity ↔ DTO
DTO               ← what crosses layer boundaries (Request in, Response out)
```

**Key design decisions:**
- Entity classes never leave the service layer — controllers only see DTOs
- All relationship resolution (FK lookups) happens in the service, not the mapper
- `FetchType.LAZY` on all `@ManyToOne` associations — no accidental eager loading
- `ResourceNotFoundException` thrown from service, caught by `GlobalExceptionHandler` — controllers stay clean
- `hasAuthority()` used in all `@PreAuthorize` expressions — roles are stored without `ROLE_` prefix
- `isProtected` flag on seeded admin account — cannot be modified via API by anyone

---

## Project Structure

```
src/main/java/org/example/employeepayroll/
├── config/               # App configuration and startup
│   ├── SecurityConfig.java       # Filter chain, BCrypt, AuthManager
│   └── DataSeeder.java           # Seeds protected admin on first startup
├── controllers/          # REST endpoints
│   ├── AuthController.java       # /auth/register, /auth/login
│   ├── EmployeeController.java
│   ├── DepartmentController.java
│   ├── UserController.java       # /users/{id}/role
│   └── HealthController.java
├── services/             # Business logic
│   ├── AuthService.java          # register + login logic
│   ├── EmployeeService.java
│   ├── DepartmentService.java
│   ├── UserService.java          # role update with protection checks
│   ├── AuthorizationService.java # isOwner() for @PreAuthorize SpEL
│   └── UserDetailsServiceImpl.java
├── repositories/         # DB access
│   ├── EmployeeRepository.java
│   ├── DepartmentRepository.java
│   └── UserRepository.java
├── entities/             # JPA entities
│   ├── Employee.java
│   ├── Department.java
│   ├── User.java
│   ├── Role.java                 # ADMIN, HR, EMPLOYEE, GUEST
│   ├── EmployeeType.java
│   └── Status.java
├── security/
│   └── UserPrincipal.java        # UserDetails wrapper around User entity
├── filters/
│   └── JwtAuthFilter.java        # OncePerRequestFilter — token extraction + validation
├── utils/
│   └── JwtUtil.java              # generateToken, validateToken, extractUsername
├── dtos/                 # Data Transfer Objects
│   ├── AuthRequestDto.java       # Used for both register and login
│   ├── AuthResponseDto.java      # Returns token + username + role
│   ├── EmployeeRequestDto.java
│   ├── EmployeeResponseDto.java
│   ├── DepartmentRequestDto.java
│   ├── DepartmentResponseDto.java
│   ├── EmployeeSummaryDto.java
│   ├── RoleUpdateDto.java
│   ├── UserResponseDto.java
│   └── ErrorResponseDto.java
├── mappers/              # Entity ↔ DTO conversion
│   ├── AuthMapper.java           # Handles BCrypt encoding on toEntity()
│   ├── EmployeeMapper.java
│   └── DepartmentMapper.java
├── specifications/       # Dynamic query filters
│   └── EmployeeSpecification.java
├── exceptions/           # Exception handling
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── DuplicateResourceException.java
└── validators/           # Custom validation
    ├── ContactNumber.java
    └── ContactNumberValidator.java
```

---

## Authentication & Authorization

### How it works

```
POST /auth/register  →  User created (GUEST by default)
                        If username matches an Employee email → auto-linked + promoted to EMPLOYEE
POST /auth/login     →  Returns signed JWT
All other endpoints  →  Require Authorization: Bearer <token>
```

### Roles

| Role | Assigned by | Access |
|---|---|---|
| `GUEST` | Default on register | No employee data access |
| `EMPLOYEE` | Auto on register if email matches an employee record | Own data only |
| `HR` | ADMIN via `PATCH /users/{id}/role` | All employee data, no delete |
| `ADMIN` | Seeded on startup or promoted by existing ADMIN | Full access |

### Endpoint access matrix

| Endpoint | ADMIN | HR | EMPLOYEE | GUEST |
|---|---|---|---|---|
| `GET /employees/` | ✓ | ✓ | ✗ | ✗ |
| `GET /employees/{id}` | ✓ | ✓ | own only | ✗ |
| `GET /employees/search` | ✓ | ✓ | ✓ | ✗ |
| `POST /employees/` | ✓ | ✓ | ✗ | ✗ |
| `PUT /employees/{id}` | ✓ | ✓ | ✗ | ✗ |
| `PATCH /employees/{id}` | ✓ | ✓ | ✗ | ✗ |
| `DELETE /employees/{id}` | ✓ | ✗ | ✗ | ✗ |
| `PATCH /users/{id}/role` | ✓ | ✗ | ✗ | ✗ |

### Admin protection rules
- An admin **cannot modify their own role**
- The **seeded admin account is protected** — no API call can modify it regardless of who makes the request. Changes to this account require direct DB access, which is intentional

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

jwt.secret=your-secret-key-must-be-at-least-32-characters
jwt.expiry=3600

app.admin.username=admin@yourcompany.com
app.admin.password=your_admin_password
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

The seeded admin account is created automatically on first startup if no admin exists.

---

## Environment Profiles

| Profile | Purpose | Activated by |
|---|---|---|
| `dev` | Local development with MySQL | Default (`spring.profiles.active=dev`) |
| `prod` | Production — reads credentials from env vars | `SPRING_PROFILES_ACTIVE=prod` |

Production expects these environment variables:
```
DB_URL              jdbc:mysql://<host>:3306/<db>
DB_USER             database username
DB_PASSWORD         database password
JWT_SECRET          signing secret (min 32 chars)
JWT_EXPIRY          token expiry in seconds
ADMIN_USERNAME      bootstrap admin email
ADMIN_PASSWORD      bootstrap admin password
```

---

## API Reference

### Auth

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Register new user | None |
| POST | `/api/v1/auth/login` | Login, returns JWT | None |

**Register / Login request:**
```json
{
  "username": "ranjan@example.com",
  "password": "yourpassword"
}
```

**Response:**
```json
{
  "token": "eyJhbGci...",
  "username": "ranjan@example.com",
  "role": "EMPLOYEE"
}
```

### Health

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/v1/health` | Application health check | None |

### Employees

| Method | Endpoint | Description | Auth | Status Codes |
|---|---|---|---|---|
| GET | `/api/v1/employees/` | List all employees (paginated) | ADMIN, HR | 200 |
| GET | `/api/v1/employees/{id}` | Get employee by ID | ADMIN, HR, owner | 200, 404 |
| GET | `/api/v1/employees/search` | Search with optional filters | ADMIN, HR, EMPLOYEE | 200 |
| POST | `/api/v1/employees/` | Create new employee | ADMIN, HR | 201, 400, 409 |
| PUT | `/api/v1/employees/{id}` | Full update (all fields) | ADMIN, HR | 200, 400, 404 |
| PATCH | `/api/v1/employees/{id}` | Partial update | ADMIN, HR | 200, 404 |
| DELETE | `/api/v1/employees/{id}` | Soft delete (status=DELETE) | ADMIN | 200, 404 |

### Departments

| Method | Endpoint | Description | Auth | Status Codes |
|---|---|---|---|---|
| GET | `/api/v1/departments/` | List all departments | Authenticated | 200 |
| GET | `/api/v1/departments/{id}` | Get department by ID | Authenticated | 200, 404 |
| POST | `/api/v1/departments/` | Create new department | ADMIN, HR | 201, 400 |
| PUT | `/api/v1/departments/{id}` | Full update | ADMIN, HR | 200, 400, 404 |
| PATCH | `/api/v1/departments/{id}` | Partial update | ADMIN, HR | 200, 404 |
| DELETE | `/api/v1/departments/{id}` | Soft delete | ADMIN | 200, 404 |

### Users

| Method | Endpoint | Description | Auth | Status Codes |
|---|---|---|---|---|
| PATCH | `/api/v1/users/{id}/role` | Update a user's role | ADMIN | 200, 400, 404 |

**Role update request:**
```json
{
  "role": "HR"
}
```

---

## Key Features

### JWT Authentication
Stateless JWT-based auth. Token is signed with HMAC-SHA and contains the username as subject. `JwtAuthFilter` runs on every request — extracts the Bearer token, validates signature and expiry, and sets the `SecurityContext`. No server-side session state.

### Role-Based Access Control
`@EnableMethodSecurity` + `@PreAuthorize` on each endpoint. Ownership check (`isOwner()`) uses the `User → Employee` link established at registration to allow employees to access only their own record.

### Auto User-Employee Linking
On registration, if the username (email) matches an existing `Employee.email`, the user is automatically linked to that employee record and promoted to `EMPLOYEE` role. No admin intervention needed for the standard onboarding flow.

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
| Duplicate resource | 409 |
| Validation failure | 400 with field-level errors |
| Bad credentials | 401 |
| Unauthorised access | 403 |
| Unexpected error | 500 |

### Input Validation
`EmployeeRequestDto` validates all fields on POST and PUT:

```
@NotBlank       → employeeCode, firstName, lastName, designation
@Email          → email
@NotNull        → dateOfJoining, employeeType, departmentId
@ContactNumber  → phoneNumber (custom: 10 digits, numbers only)
```

---

## Data Model

```
User
  id            BIGINT PK AUTO_INCREMENT
  username      VARCHAR UNIQUE (email format)
  password      VARCHAR (BCrypt hash)
  role          ENUM(ADMIN, HR, EMPLOYEE, GUEST)  DEFAULT GUEST
  isProtected   BOOLEAN  DEFAULT FALSE
  employee_id   FK → Employee.id (nullable — GUEST users have no link)

Department
  id          BIGINT PK AUTO_INCREMENT
  name        VARCHAR
  code        VARCHAR
  status      ENUM(ACTIVE, INACTIVE, HOLD, DELETE)

Employee
  id              BIGINT PK AUTO_INCREMENT
  employeeCode    VARCHAR
  firstName       VARCHAR
  lastName        VARCHAR
  email           VARCHAR UNIQUE
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
- `User → Employee`: `@OneToOne` — one system user linked to one employee record
- `Employee → Department`: `@ManyToOne` — many employees belong to one department
- `Employee → Employee`: self-referencing `@ManyToOne` for manager hierarchy
- All `@ManyToOne` associations use `FetchType.LAZY`

---

## Sample Requests

**Register (matches existing employee email — auto-linked as EMPLOYEE)**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "ranjan@example.com", "password": "password123"}'
```

**Login**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "ranjan@example.com", "password": "password123"}'
```

**Create a department (HR or ADMIN token required)**
```bash
curl -X POST http://localhost:8080/api/v1/departments/ \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"name": "Engineering", "code": "ENG"}'
```

**Create an employee (HR or ADMIN token required)**
```bash
curl -X POST http://localhost:8080/api/v1/employees/ \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
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

**Promote a user to HR (ADMIN token required)**
```bash
curl -X PATCH http://localhost:8080/api/v1/users/2/role \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"role": "HR"}'
```

**Search employees**
```bash
curl "http://localhost:8080/api/v1/employees/search?name=ranjan&status=ACTIVE&page=0&size=5" \
  -H "Authorization: Bearer <token>"
```

---

## What's Next

- [ ] `@Transactional` — salary revision logic with rollback on failure
- [ ] N+1 fix — `@EntityGraph` and `JOIN FETCH` for optimised queries
- [ ] Redis caching — `@Cacheable` on frequently read data
- [ ] Flyway — SQL migration files replacing `ddl-auto=update`
- [ ] Unit tests — JUnit 5 + Mockito for service layer
- [ ] Integration tests — `@SpringBootTest` with real DB
- [ ] Swagger / OpenAPI — auto-generated API documentation
- [ ] Docker + docker-compose — containerised local setup
- [ ] AWS deployment — EC2 + RDS + GitHub Actions CI/CD
