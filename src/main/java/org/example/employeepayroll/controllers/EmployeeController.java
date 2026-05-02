package org.example.employeepayroll.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.employeepayroll.dtos.EmployeeRequestDto;
import org.example.employeepayroll.dtos.EmployeeResponseDto;
import org.example.employeepayroll.dtos.ErrorResponseDto;
import org.example.employeepayroll.entities.Status;
import org.example.employeepayroll.services.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employee", description = "Employee management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // ── Search ────────────────────────────────────────────────────────────────

    @Operation(summary = "Search employees", description = "Filter employees by name, department, and/or status. All parameters are optional. Accessible by ADMIN, HR, and EMPLOYEE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results returned",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient role",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('HR') or hasAuthority('EMPLOYEE')")
    public ResponseEntity<?> search(
            @Parameter(description = "Partial first or last name filter") @RequestParam(required = false) String name,
            @Parameter(description = "Department name filter") @RequestParam(required = false) String dept,
            @Parameter(description = "Status filter (ACTIVE / INACTIVE)") @RequestParam(required = false) Status status,
            Pageable pageable) {
        Page<EmployeeResponseDto> results = employeeService.searchEmployees(name, dept, status, pageable);
        return ResponseEntity.ok(results);
    }

    // ── Get All ───────────────────────────────────────────────────────────────

    @Operation(summary = "Get all employees", description = "Returns a paginated list of all employees. Accessible by ADMIN and HR.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee list returned",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient role",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('HR')")
    public ResponseEntity<?> getAllEmployees(Pageable pageable) {
        Page<EmployeeResponseDto> employeeList = employeeService.getEmployees(pageable);
        return ResponseEntity.ok(employeeList);
    }

    // ── Get By Id ─────────────────────────────────────────────────────────────

    @Operation(summary = "Get employee by ID", description = "Returns a single employee by their ID. ADMIN and HR can fetch any employee. EMPLOYEE can only fetch their own record.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee found",
                    content = @Content(schema = @Schema(implementation = EmployeeResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient role or not the record owner",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('HR') or @authorizationService.isOwner(#id)")
    public ResponseEntity<?> getEmployee(
            @Parameter(description = "Employee ID", required = true) @PathVariable Long id) {
        EmployeeResponseDto employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    // ── Create ────────────────────────────────────────────────────────────────

    @Operation(summary = "Create employee", description = "Creates a new employee record. Requires ADMIN or HR role.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Employee created successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed — check the error message for field details",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient role",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('HR')")
    public ResponseEntity<?> addEmployee(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Employee details to create", required = true,
                    content = @Content(schema = @Schema(implementation = EmployeeRequestDto.class)))
            @Valid @RequestBody EmployeeRequestDto employeeRequestDto) {
        EmployeeResponseDto newEmployee = employeeService.addEmployee(employeeRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newEmployee);
    }

    // ── Full Update ───────────────────────────────────────────────────────────

    @Operation(summary = "Full update employee", description = "Replaces all fields of an existing employee (PUT semantics). Requires ADMIN or HR role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee updated",
                    content = @Content(schema = @Schema(implementation = EmployeeResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient role",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('HR')")
    public ResponseEntity<?> updateEmployee(
            @Parameter(description = "Employee ID", required = true) @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDto employeeRequestDto) {
        EmployeeResponseDto employee = employeeService.putEmployee(id, employeeRequestDto);
        return ResponseEntity.ok(employee);
    }

    // ── Partial Update ────────────────────────────────────────────────────────

    @Operation(summary = "Partial update employee", description = "Updates only the provided fields (PATCH semantics). Null fields are ignored. Requires ADMIN or HR role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee patched",
                    content = @Content(schema = @Schema(implementation = EmployeeResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient role",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('HR')")
    public ResponseEntity<?> patchEmployee(
            @Parameter(description = "Employee ID", required = true) @PathVariable Long id,
            @RequestBody EmployeeRequestDto employeeRequestDto) {
        EmployeeResponseDto employee = employeeService.patchEmployee(id, employeeRequestDto);
        return ResponseEntity.ok(employee);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Operation(summary = "Delete employee", description = "Soft-deletes an employee by ID (sets status to INACTIVE). Requires ADMIN role only.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee deleted",
                    content = @Content(schema = @Schema(implementation = EmployeeResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient role — ADMIN only",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteEmployee(
            @Parameter(description = "Employee ID", required = true) @PathVariable Long id) {
        EmployeeResponseDto employee = employeeService.deleteEmployeeById(id);
        return ResponseEntity.ok(employee);
    }
}