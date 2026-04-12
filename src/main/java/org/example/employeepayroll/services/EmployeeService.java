package org.example.employeepayroll.services;

import org.example.employeepayroll.dtos.EmployeeRequestDto;
import org.example.employeepayroll.dtos.EmployeeResponseDto;
import org.example.employeepayroll.entities.Department;
import org.example.employeepayroll.entities.Employee;
import org.example.employeepayroll.entities.Status;
import org.example.employeepayroll.exceptions.ResourceNotFoundException;
import org.example.employeepayroll.mappers.EmployeeMapper;
import org.example.employeepayroll.repositories.DepartmentRepository;
import org.example.employeepayroll.repositories.EmployeeRepository;
import org.example.employeepayroll.specifications.EmployeeSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final DepartmentRepository departmentRepository;

    public EmployeeService(EmployeeRepository employeeRepository, EmployeeMapper employeeMapper, DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
        this.departmentRepository = departmentRepository;
    }

    public EmployeeResponseDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Employee with id: " + id + " not found")
        );
        return employeeMapper.toResponse(employee);
    }

    public EmployeeResponseDto addEmployee(EmployeeRequestDto employeeRequestDto) {
        Department department = departmentRepository.findById(employeeRequestDto.getDepartmentId()).orElseThrow(
                () -> new ResourceNotFoundException("Department with id: " + employeeRequestDto.getDepartmentId() + " not found")
        );
        Employee employee = employeeMapper.toEntity(employeeRequestDto);
        employee.setDepartment(department);
        employeeRepository.save(employee);
        return employeeMapper.toResponse(employee);
    }

    public EmployeeResponseDto putEmployee(Long id, EmployeeRequestDto employeeRequestDto) {
        Employee employee = employeeRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Employee with id: " + id + " not found")
        );
        Department department = departmentRepository.findById(employeeRequestDto.getDepartmentId()).orElseThrow(
                () -> new ResourceNotFoundException("Department with id: " + id + " not found")
        );
        employeeMapper.updateEntity(employeeRequestDto, employee);
        employee.setDepartment(department);
        employee = employeeRepository.save(employee);
        return employeeMapper.toResponse(employee);
    }

    public EmployeeResponseDto patchEmployee(Long id, EmployeeRequestDto employeeRequestDto) {
        Employee employee = employeeRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Employee with id: " + id + " not found")
        );
        applyPatch(employeeRequestDto, employee);
        if(employeeRequestDto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(employeeRequestDto.getDepartmentId()).orElseThrow(
                    () -> new ResourceNotFoundException("Department with id: " + employeeRequestDto.getDepartmentId() + " not found")
            );
            employee.setDepartment(department);
        }
        employee = employeeRepository.save(employee);
        return employeeMapper.toResponse(employee);
    }

    private void applyPatch(EmployeeRequestDto employeeRequestDto, Employee employee) {
        if(employeeRequestDto.getEmployeeCode() != null) employee.setEmployeeCode(employeeRequestDto.getEmployeeCode());
        if(employeeRequestDto.getFirstName() != null) employee.setFirstName(employeeRequestDto.getFirstName());
        if(employeeRequestDto.getLastName() != null) employee.setLastName(employeeRequestDto.getLastName());
        if(employeeRequestDto.getEmail() != null) employee.setEmail(employeeRequestDto.getEmail());
        if(employeeRequestDto.getPhoneNumber() != null) employee.setPhoneNumber(employeeRequestDto.getPhoneNumber());
        if(employeeRequestDto.getDesignation() != null) employee.setDesignation(employeeRequestDto.getDesignation());
        if(employeeRequestDto.getEmployeeType() != null) employee.setEmployeeType(employeeRequestDto.getEmployeeType());
        if(employeeRequestDto.getDateOfJoining() != null) employee.setDateOfJoining(employeeRequestDto.getDateOfJoining());
    }

    public EmployeeResponseDto deleteEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Employee with id: " + id + " not found")
        );
        employee.setStatus(Status.DELETE);
        employee = employeeRepository.save(employee);
        return employeeMapper.toResponse(employee);
    }

    public Page<EmployeeResponseDto> getEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(employeeMapper::toResponse);
    }

    public Page<EmployeeResponseDto> searchEmployees(String name, String dept, Status status, Pageable pageable) {
        Specification<Employee> spec = Specification
                .where(EmployeeSpecification.hasName(name))
                .and(EmployeeSpecification.hasDepartment(dept))
                .and(EmployeeSpecification.hasStatus(status));

        return employeeRepository.findAll(spec, pageable)
                .map(employeeMapper::toResponse);
    }
}
