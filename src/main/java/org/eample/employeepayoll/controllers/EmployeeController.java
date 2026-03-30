package org.eample.employeepayoll.controllers;

import org.eample.employeepayoll.dtos.EmployeeRequestDto;
import org.eample.employeepayoll.dtos.EmployeeResponseDto;
import org.eample.employeepayoll.entities.Employee;
import org.eample.employeepayoll.services.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/")
    public ResponseEntity<?> getAllEmployees() {
        Collection<EmployeeResponseDto> employeeList = employeeService.getEmployees();
        return ResponseEntity.ok(employeeList);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEmployee(@PathVariable Long id) {
        if(!employeeService.existsById(id)) return ResponseEntity.notFound().build();
        EmployeeResponseDto employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @PostMapping("/")
    public ResponseEntity<?> addEmployee(@RequestBody EmployeeRequestDto employeeRequestDto) {
        EmployeeResponseDto newEmployee = employeeService.addEmployee(employeeRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newEmployee);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id, @RequestBody EmployeeRequestDto employeeRequestDto) {
        if(!employeeService.existsById(id)) return ResponseEntity.notFound().build();
        EmployeeResponseDto employee = employeeService.patchEmployee(id, employeeRequestDto);
        return ResponseEntity.ok(employee);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patchEmployee(@PathVariable Long id, @RequestBody EmployeeRequestDto employeeRequestDto) {
        if(!employeeService.existsById(id)) return ResponseEntity.notFound().build();
        EmployeeResponseDto employee = employeeService.patchEmployee(id, employeeRequestDto);
        return ResponseEntity.ok(employee);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        if(!employeeService.existsById(id)) return ResponseEntity.notFound().build();
        EmployeeResponseDto employee = employeeService.deleteEmployeeById(id);
        return ResponseEntity.ok(employee);
    }
}
