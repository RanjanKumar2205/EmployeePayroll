package org.eample.employeepayoll.services;

import org.eample.employeepayoll.dtos.EmployeeRequestDto;
import org.eample.employeepayoll.dtos.EmployeeResponseDto;
import org.eample.employeepayoll.entities.Department;
import org.eample.employeepayoll.entities.Employee;
import org.eample.employeepayoll.entities.Status;
import org.eample.employeepayoll.exceptions.ResourceNotFoundException;
import org.eample.employeepayoll.mappers.EmployeeMapper;
import org.eample.employeepayoll.repositories.DepartmentRepository;
import org.eample.employeepayoll.repositories.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

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
        if(employeeRequestDto.getDepartmentId() != null) employee.setDepartment(departmentRepository.findById(employeeRequestDto.getDepartmentId()).orElseThrow(
                () -> new ResourceNotFoundException("Department with id: " + employeeRequestDto.getDepartmentId() + " not found")
        ));
    }

    public EmployeeResponseDto deleteEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Employee with id: " + id + " not found")
        );
        employee.setStatus(Status.DELETE);
        employee = employeeRepository.save(employee);
        return employeeMapper.toResponse(employee);
    }

    public Collection<EmployeeResponseDto> getEmployees() {
        Collection<Employee> employeeList = employeeRepository.findAll();
        Collection<EmployeeResponseDto> employeeResponseDtoList = new ArrayList<>();
        for (Employee employee : employeeList) {
            employeeResponseDtoList.add(employeeMapper.toResponse(employee));
        }
        return employeeResponseDtoList;
    }
}
