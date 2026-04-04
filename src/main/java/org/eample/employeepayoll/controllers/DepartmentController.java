package org.eample.employeepayoll.controllers;

import jakarta.validation.Valid;
import org.eample.employeepayoll.dtos.DepartmentRequestDto;
import org.eample.employeepayoll.dtos.DepartmentResponseDto;
import org.eample.employeepayoll.services.DepartmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping("/")
    public ResponseEntity<?> getAllDepartments() {
        Collection<DepartmentResponseDto> departmentList = departmentService.getDepartments();
        return ResponseEntity.ok(departmentList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDepartment(@PathVariable Long id) {
        DepartmentResponseDto department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(department);
    }

    @PostMapping("/")
    public ResponseEntity<?> addDepartment(@Valid @RequestBody DepartmentRequestDto departmentRequestDto) {
        DepartmentResponseDto newDepartment = departmentService.addDepartment(departmentRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newDepartment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentRequestDto departmentRequestDto) {
        DepartmentResponseDto department = departmentService.patchDepartment(id, departmentRequestDto);
        return ResponseEntity.ok(department);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patchDepartment(@PathVariable Long id, @RequestBody DepartmentRequestDto departmentRequestDto) {
        DepartmentResponseDto department = departmentService.patchDepartment(id, departmentRequestDto);
        return ResponseEntity.ok(department);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        DepartmentResponseDto department = departmentService.deleteDepartmentById(id);
        return ResponseEntity.ok(department);
    }
}
