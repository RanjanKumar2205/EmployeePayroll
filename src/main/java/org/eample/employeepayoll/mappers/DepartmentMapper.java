package org.eample.employeepayoll.mappers;

import org.eample.employeepayoll.dtos.DepartmentRequestDto;
import org.eample.employeepayoll.dtos.DepartmentResponseDto;
import org.eample.employeepayoll.entities.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {
    public Department toEntity(DepartmentRequestDto departmentRequestDto) {
        return Department.builder()
                .name(departmentRequestDto.getName())
                .code(departmentRequestDto.getCode())
                .build();
    }

    public DepartmentResponseDto toResponse(Department department) {
//        Collection<EmployeeSummaryDto> employeeSummaries = department.getEmployee()
//                .stream()
//                .map(emp -> EmployeeSummaryDto.builder()
//                        .id(emp.getId())
//                        .employeeCode(emp.getEmployeeCode())
//                        .firstName(emp.getFirstName())
//                        .lastName(emp.getLastName())
//                        .designation(emp.getDesignation())
//                        .build())
//                .toList();

        return DepartmentResponseDto.builder()
                .id(department.getId())
                .name(department.getName())
                .code(department.getCode())
                .status(department.getStatus())
//                .employee(employeeSummaries)
                .build();
    }

    public void updateEntity(DepartmentRequestDto departmentRequestDto, Department department) {
        department.setName(departmentRequestDto.getName());
        department.setCode(departmentRequestDto.getCode());
    }
}
