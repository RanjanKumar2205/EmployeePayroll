package org.eample.employeepayoll.controllers;

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
    public ResponseEntity<Collection<Employee>> getAllEmployees() {
        Collection<Employee> employeeList = employeeService.getEmployees();
        return ResponseEntity.ok(employeeList);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEmployee(@PathVariable Long id) {
        Optional<Employee> employee = employeeService.getEmployeeById(id);
        if (employee.isPresent()) return ResponseEntity.ok(employee.get());
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/")
    public ResponseEntity<?> addEmployee(@RequestBody Employee employee) {
        Optional<Employee> employeeById = employeeService.getEmployeeById(employee.getId());
        if(employeeById.isPresent()) return ResponseEntity.status(HttpStatus.CONFLICT).body("Employee with given id exists!");
        Employee newEmployee = employeeService.addEmployee(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(newEmployee);
    }

    @PutMapping("/")
    public ResponseEntity<?> updateEmployee(@RequestBody Employee employee) {
        Employee updatedEmployee;
        if(employeeService.getEmployeeById(employee.getId()).isEmpty()) return ResponseEntity.notFound().build();
        else updatedEmployee = employeeService.patchEmployee(employee);
        return ResponseEntity.ok(updatedEmployee);
    }

    @PatchMapping("/")
    public ResponseEntity<?> patchEmployee(@RequestBody Employee employee) {
        if(employeeService.getEmployeeById(employee.getId()).isEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Employee with given id does not exist!");
        employeeService.patchEmployee(employee);
        return ResponseEntity.ok(employee);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        Optional<Employee> employee = employeeService.getEmployeeById(id);
        if(employee.isEmpty()) return ResponseEntity.notFound().build();
        employeeService.deleteEmployeeById(id);
        return ResponseEntity.ok(employee.get());
    }
}
