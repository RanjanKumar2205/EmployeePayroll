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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import static org.example.employeepayroll.services.DepartmentService.DEPARTMENT_NOT_FOUND;

@Service
public class EmployeeService {
    public static final String EMPLOYEE_NOT_FOUND = "Employee with id: %s not found";
    public static final String MANAGER_NOT_FOUND = "Manager with id: %s not found";
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final DepartmentRepository departmentRepository;
    private final AuditLogService auditLogService;

    public EmployeeService(EmployeeRepository employeeRepository, EmployeeMapper employeeMapper, DepartmentRepository departmentRepository, AuditLogService auditLogService) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
        this.departmentRepository = departmentRepository;
        this.auditLogService = auditLogService;
    }

    @Cacheable(value="employees", key="#id")
    public EmployeeResponseDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(String.format(EMPLOYEE_NOT_FOUND, id))
        );
        return employeeMapper.toResponse(employee);
    }

    public EmployeeResponseDto addEmployee(EmployeeRequestDto employeeRequestDto) {
        Department department = departmentRepository.findById(employeeRequestDto.getDepartmentId()).orElseThrow(
                () -> new ResourceNotFoundException(String.format(DEPARTMENT_NOT_FOUND, employeeRequestDto.getDepartmentId()))
        );
        Employee manager = resolveManager(employeeRequestDto.getManagerId());
        Employee employee = employeeMapper.toEntity(employeeRequestDto);
        employee.setDepartment(department);
        employee.setManager(manager);
        employeeRepository.save(employee);

        auditLogService.logCreate(Employee.class.getSimpleName(), employee.getId());

        return employeeMapper.toResponse(employee);
    }

    @CacheEvict(value="employees", key="#id")
    public EmployeeResponseDto putEmployee(Long id, EmployeeRequestDto employeeRequestDto) {
        Employee existing = employeeRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(String.format(EMPLOYEE_NOT_FOUND, id))
        );
        Department department = departmentRepository.findById(employeeRequestDto.getDepartmentId()).orElseThrow(
                () -> new ResourceNotFoundException(String.format(DEPARTMENT_NOT_FOUND, id))
        );
        Employee manager = resolveManager(employeeRequestDto.getManagerId());
        // Snapshot scalar fields + FK IDs before mutation
        Employee oldSnapshot = existing.snapshot();
        Long oldDepartmentId = existing.getDepartment() != null
                ? existing.getDepartment().getId() : null;
        Long oldManagerId = existing.getManager() != null
                ? existing.getManager().getId() : null;

        employeeMapper.updateEntity(employeeRequestDto, existing);
        existing.setDepartment(department);
        existing.setManager(manager);
        Employee updated = employeeRepository.save(existing);

        // Diff scalars via reflection
        auditLogService.logChanges(
                Employee.class.getSimpleName(), updated.getId(),
                oldSnapshot, updated.snapshot());

        // Diff FK IDs manually
        Long newDepartmentId = updated.getDepartment() != null
                ? updated.getDepartment().getId() : null;
        auditLogService.logFkChange(
                Employee.class.getSimpleName(), updated.getId(),
                "departmentId", oldDepartmentId, newDepartmentId);

        Long newManagerId = updated.getManager() != null ? updated.getManager().getId() : null;
        auditLogService.logFkChange(
                Employee.class.getSimpleName(), updated.getId(),
                "managerId", oldManagerId, newManagerId);

        return employeeMapper.toResponse(updated);
    }
    @CacheEvict(value="employees", key="#id")
    public EmployeeResponseDto patchEmployee(Long id, EmployeeRequestDto employeeRequestDto) {
        Employee existing = employeeRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(String.format(EMPLOYEE_NOT_FOUND, id))
        );

        // Snapshot before mutation
        Employee oldSnapshot = existing.snapshot();
        Long oldDepartmentId = existing.getDepartment() != null
                ? existing.getDepartment().getId() : null;
        Long oldManagerId = existing.getManager() != null
                ? existing.getManager().getId() : null;

        applyPatch(employeeRequestDto, existing);

        if (employeeRequestDto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(employeeRequestDto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            String.format(DEPARTMENT_NOT_FOUND, employeeRequestDto.getDepartmentId())));
            existing.setDepartment(department);
        }

        if (employeeRequestDto.getManagerId() != null) {
            existing.setManager(resolveManager(employeeRequestDto.getManagerId()));
        }

        Employee updated = employeeRepository.save(existing);

        // Diff scalars
        auditLogService.logChanges(
                Employee.class.getSimpleName(), updated.getId(),
                oldSnapshot, updated.snapshot());

        // Diff department FK only if it was in the patch request
        if (employeeRequestDto.getDepartmentId() != null) {
            Long newDepartmentId = updated.getDepartment() != null
                    ? updated.getDepartment().getId() : null;

            auditLogService.logFkChange(
                    Employee.class.getSimpleName(), updated.getId(),
                    "departmentId", oldDepartmentId, newDepartmentId);
        }

        if (employeeRequestDto.getManagerId() != null) {
            Long newManagerId = updated.getManager() != null
                    ? updated.getManager().getId() : null;

            auditLogService.logFkChange(
                    Employee.class.getSimpleName(), updated.getId(),
                    "managerId", oldManagerId, newManagerId);
        }

        return employeeMapper.toResponse(updated);
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

    @CacheEvict(value="employees", key="#id")
    public EmployeeResponseDto deleteEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(String.format(EMPLOYEE_NOT_FOUND, id))
        );
        employee.setStatus(Status.DELETE);
        employee = employeeRepository.save(employee);

        auditLogService.logDelete(Employee.class.getSimpleName(), id);

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

    private Employee resolveManager(Long managerId) {
        if (managerId == null) return null;
        return employeeRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(MANAGER_NOT_FOUND, managerId)));
    }
}
