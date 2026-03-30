package org.eample.employeepayoll.services;

import org.eample.employeepayoll.dtos.EmployeeRequestDto;
import org.eample.employeepayoll.dtos.EmployeeResponseDto;
import org.eample.employeepayoll.entities.Employee;
import org.eample.employeepayoll.entities.EmployeeStatus;
import org.eample.employeepayoll.mappers.EmployeeMapper;
import org.eample.employeepayoll.repositories.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    public EmployeeService(EmployeeRepository employeeRepository, EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
    }

    public EmployeeResponseDto getEmployeeById(Long id) {
        Optional<Employee> employee = employeeRepository.findById(id);
        return employeeMapper.toEmployeeResponseDto(employee.get());
    }

    public EmployeeResponseDto addEmployee(EmployeeRequestDto employeeRequestDto) {
        Employee employee = employeeMapper.toEmployee(employeeRequestDto);
        employeeRepository.save(employee);
        return employeeMapper.toEmployeeResponseDto(employee);
    }

    public EmployeeResponseDto patchEmployee(Long id, EmployeeRequestDto employeeRequestDto) {
        Optional<Employee> fetchEmployee = employeeRepository.findById(id);
        if (fetchEmployee.isEmpty()) return null;
        Employee employee = fetchEmployee.get();
        if(employeeRequestDto.getEmployeeCode() != null) employee.setEmployeeCode(employeeRequestDto.getEmployeeCode());
        if(employeeRequestDto.getFirstName() != null) employee.setFirstName(employeeRequestDto.getFirstName());
        if(employeeRequestDto.getLastName() != null) employee.setLastName(employeeRequestDto.getLastName());
        if(employeeRequestDto.getEmail() != null) employee.setEmail(employeeRequestDto.getEmail());
        if(employeeRequestDto.getPhoneNumber() != null) employee.setPhoneNumber(employeeRequestDto.getPhoneNumber());
        if(employeeRequestDto.getDesignation() != null) employee.setDesignation(employeeRequestDto.getDesignation());
        if(employeeRequestDto.getEmployeeType() != null) employee.setEmployeeType(employeeRequestDto.getEmployeeType());
        employee = employeeRepository.save(employee);
        return employeeMapper.toEmployeeResponseDto(employee);
    }

    public EmployeeResponseDto deleteEmployeeById(Long id) {
        Optional<Employee> fetchEmployee = employeeRepository.findById(id);
        if (fetchEmployee.isEmpty()) return null;
        Employee employee = fetchEmployee.get();
        employee.setStatus(EmployeeStatus.DELETE);
        employee = employeeRepository.save(employee);
        return employeeMapper.toEmployeeResponseDto(employee);
    }

    public Collection<EmployeeResponseDto> getEmployees() {
        Collection<Employee> employeeList = employeeRepository.findAll();
        Collection<EmployeeResponseDto> employeeResponseDtoList = new ArrayList<>();
        for (Employee employee : employeeList) {
            employeeResponseDtoList.add(employeeMapper.toEmployeeResponseDto(employee));
        }
        return employeeResponseDtoList;
    }

    public boolean existsById(Long id) {
        return employeeRepository.existsById(id);
    }
}
