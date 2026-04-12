package org.example.employeepayroll.mappers;

import org.example.employeepayroll.dtos.EmployeeRequestDto;
import org.example.employeepayroll.dtos.EmployeeResponseDto;
import org.example.employeepayroll.entities.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {
    public Employee toEntity(EmployeeRequestDto employeeRequestDto) {
        return Employee.builder()
                .employeeCode(employeeRequestDto.getEmployeeCode())
                .firstName(employeeRequestDto.getFirstName())
                .lastName(employeeRequestDto.getLastName())
                .email(employeeRequestDto.getEmail())
                .phoneNumber(employeeRequestDto.getPhoneNumber())
                .designation(employeeRequestDto.getDesignation())
                .employeeType(employeeRequestDto.getEmployeeType())
                .dateOfJoining(employeeRequestDto.getDateOfJoining())
                .build();
    }

    public EmployeeResponseDto toResponse(Employee employee) {
        return EmployeeResponseDto.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .dateOfJoining(employee.getDateOfJoining())
                .designation(employee.getDesignation())
                .employeeType(employee.getEmployeeType())
                .status(employee.getStatus())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .departmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null)
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .build();
    }

    public void updateEntity(EmployeeRequestDto employeeRequestDto, Employee employee) {
        employee.setEmployeeCode(employeeRequestDto.getEmployeeCode());
        employee.setFirstName(employeeRequestDto.getFirstName());
        employee.setLastName(employeeRequestDto.getLastName());
        employee.setEmail(employeeRequestDto.getEmail());
        employee.setPhoneNumber(employeeRequestDto.getPhoneNumber());
        employee.setDesignation(employeeRequestDto.getDesignation());
        employee.setEmployeeType(employeeRequestDto.getEmployeeType());
        employee.setDateOfJoining(employeeRequestDto.getDateOfJoining());
    }
}
