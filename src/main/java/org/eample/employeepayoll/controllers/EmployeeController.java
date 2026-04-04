package org.eample.employeepayoll.controllers;

import jakarta.validation.Valid;
import org.eample.employeepayoll.dtos.EmployeeRequestDto;
import org.eample.employeepayoll.dtos.EmployeeResponseDto;
import org.eample.employeepayoll.entities.Status;
import org.eample.employeepayoll.services.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(required = false) String name,
                                    @RequestParam(required = false) String dept,
                                    @RequestParam(required = false) Status status,
                                    Pageable pageable) {
        Page<EmployeeResponseDto> results = employeeService.searchEmployees(name, dept, status, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/")
    public ResponseEntity<?> getAllEmployees(Pageable pageable) {
        Page<EmployeeResponseDto> employeeList = employeeService.getEmployees(pageable);
        return ResponseEntity.ok(employeeList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployee(@PathVariable Long id) {
        EmployeeResponseDto employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @PostMapping("/")
    public ResponseEntity<?> addEmployee(@Valid @RequestBody EmployeeRequestDto employeeRequestDto) {
        EmployeeResponseDto newEmployee = employeeService.addEmployee(employeeRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newEmployee);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequestDto employeeRequestDto) {
        EmployeeResponseDto employee = employeeService.putEmployee(id, employeeRequestDto);
        return ResponseEntity.ok(employee);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patchEmployee(@PathVariable Long id, @RequestBody EmployeeRequestDto employeeRequestDto) {
        EmployeeResponseDto employee = employeeService.patchEmployee(id, employeeRequestDto);
        return ResponseEntity.ok(employee);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        EmployeeResponseDto employee = employeeService.deleteEmployeeById(id);
        return ResponseEntity.ok(employee);
    }
}
