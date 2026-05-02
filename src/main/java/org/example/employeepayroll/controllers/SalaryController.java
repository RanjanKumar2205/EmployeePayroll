package org.example.employeepayroll.controllers;

import jakarta.validation.Valid;
import org.example.employeepayroll.dtos.DepartmentRequestDto;
import org.example.employeepayroll.dtos.DepartmentResponseDto;
import org.example.employeepayroll.dtos.SalaryRequestDto;
import org.example.employeepayroll.dtos.SalaryResponseDto;
import org.example.employeepayroll.services.DepartmentService;
import org.example.employeepayroll.services.SalaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/v1/salaryStructure")
public class SalaryController {

    private final SalaryService salaryService;

    public SalaryController(SalaryService salaryService) {
        this.salaryService = salaryService;
    }

    @GetMapping("/")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('HR') or @authorizationService.isOwner(#employeeId)")
    public ResponseEntity<?> getAllSalaries(@RequestParam(required = false) @P("employeeId") Long employeeId) {
        if(employeeId != null){
            return ResponseEntity.ok(salaryService.getSalariesByEmployeeId(employeeId));
        }
        return ResponseEntity.ok(salaryService.getSalaries());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('HR')")
    public ResponseEntity<?> getSalary(@PathVariable Long id) {
        SalaryResponseDto salaryList = salaryService.getSalaryById(id);
        return ResponseEntity.ok(salaryList);
    }

    @PostMapping("/")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('HR')")
    public ResponseEntity<?> addSalary(@Valid @RequestBody SalaryRequestDto salaryRequestDto) {
        SalaryResponseDto newSalary = salaryService.addSalary(salaryRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newSalary);
    }

    @PostMapping("/revise/{employeeId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('HR')")
    public ResponseEntity<?> reviseSalary(@PathVariable Long employeeId,
                                          @Valid @RequestBody SalaryRequestDto dto) {
        SalaryResponseDto revised = salaryService.reviseSalary(employeeId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(revised);
    }
}
