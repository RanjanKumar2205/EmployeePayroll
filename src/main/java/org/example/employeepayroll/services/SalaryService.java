package org.example.employeepayroll.services;

import jakarta.validation.Valid;
import org.example.employeepayroll.dtos.SalaryRequestDto;
import org.example.employeepayroll.dtos.SalaryResponseDto;
import org.example.employeepayroll.entities.Employee;
import org.example.employeepayroll.entities.SalaryStructure;
import org.example.employeepayroll.exceptions.DuplicateResourceException;
import org.example.employeepayroll.exceptions.ResourceNotFoundException;
import org.example.employeepayroll.mappers.SalaryMapper;
import org.example.employeepayroll.repositories.EmployeeRepository;
import org.example.employeepayroll.repositories.SalaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;

import static org.example.employeepayroll.services.EmployeeService.EMPLOYEE_NOT_FOUND;

@Service
public class SalaryService {
    private final SalaryRepository salaryRepository;
    private final SalaryMapper salaryMapper;
    private final EmployeeRepository employeeRepository;
    public SalaryService(SalaryRepository salaryRepository, SalaryMapper salaryMapper, EmployeeRepository employeeRepository) {
        this.salaryRepository = salaryRepository;
        this.salaryMapper = salaryMapper;
        this.employeeRepository = employeeRepository;
    }

    public Collection<SalaryResponseDto> getSalaries() {
        return salaryRepository.findAll()
                .stream()
                .map(salaryMapper::toResponse)
                .toList();
    }

    public Collection<SalaryResponseDto> getSalariesByEmployeeId(Long id) {
        return salaryRepository.findByEmployeeId(id)
                .stream()
                .map(salaryMapper::toResponse)
                .toList();
    }

    public SalaryResponseDto getSalaryById(Long id) {
        SalaryStructure salary = salaryRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Salary with id: " + id + " not found")
        );
        return salaryMapper.toResponse(salary);
    }

    public SalaryResponseDto addSalary(@Valid SalaryRequestDto salaryRequestDto) {
        Employee employee = employeeRepository.findById(salaryRequestDto.getEmployeeId()).orElseThrow(
                () -> new ResourceNotFoundException(String.format(EMPLOYEE_NOT_FOUND, salaryRequestDto.getEmployeeId()))
        );
        salaryRepository.findByEmployeeIdAndIsActiveTrue(salaryRequestDto.getEmployeeId())
                .ifPresent(s -> {
                    throw new DuplicateResourceException(
                            "Active salary already exists for employee: " + salaryRequestDto.getEmployeeId()
                                    + ". Use the revise endpoint instead.");
                });
        SalaryStructure salary = salaryMapper.toEntity(salaryRequestDto, employee);
        salaryRepository.save(salary);
        return salaryMapper.toResponse(salary);
    }

    @Transactional
    public SalaryResponseDto reviseSalary(Long employeeId, SalaryRequestDto dto) {
        SalaryStructure current = salaryRepository
                .findByEmployeeIdAndIsActiveTrue(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active salary found for employee: " + employeeId));

        current.setActive(false);
        current.setEffectiveTo(LocalDate.now());
        salaryRepository.save(current);

        Employee employee = current.getEmployee();
        SalaryStructure newSalary = salaryMapper.toEntity(dto, employee);
        newSalary = salaryRepository.save(newSalary);

        return salaryMapper.toResponse(newSalary);
    }
}
