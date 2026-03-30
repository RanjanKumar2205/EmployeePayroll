package org.eample.employeepayoll.mappers;

import org.eample.employeepayoll.dtos.EmployeeRequestDto;
import org.eample.employeepayoll.dtos.EmployeeResponseDto;
import org.eample.employeepayoll.entities.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {
    public Employee toEmployee(EmployeeRequestDto employeeRequestDto) {
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

    public EmployeeResponseDto toEmployeeResponseDto(Employee employee) {
        return EmployeeResponseDto.builder()
                .employeeCode(employee.getEmployeeCode())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .dateOfJoining(employee.getDateOfJoining())
                .designation(employee.getDesignation())
                .employeeType(employee.getEmployeeType())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }
}
