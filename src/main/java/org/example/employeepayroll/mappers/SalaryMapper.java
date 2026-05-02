package org.example.employeepayroll.mappers;

import org.example.employeepayroll.dtos.SalaryRequestDto;
import org.example.employeepayroll.dtos.SalaryResponseDto;
import org.example.employeepayroll.entities.Employee;
import org.example.employeepayroll.entities.SalaryStructure;
import org.springframework.stereotype.Component;

@Component
public class SalaryMapper {

    public SalaryResponseDto toResponse(SalaryStructure salary) {
        return SalaryResponseDto.builder()
                .id(salary.getId())
                .employeeId(salary.getEmployee().getId())
                .employeeName(salary.getEmployee().getFirstName() + " " + salary.getEmployee().getLastName())
                .basicSalary(salary.getBasicSalary())
                .hra(salary.getHra())
                .specialAllowance(salary.getSpecialAllowance())
                .pfEmployee(salary.getPfEmployee())
                .pfEmployer(salary.getPfEmployer())
                .professionalTax(salary.getProfessionalTax())
                .tds(salary.getTds())
                .effectiveFrom(salary.getEffectiveFrom())
                .effectiveTo(salary.getEffectiveTo())
                .isActive(salary.isActive())
                .build();
    }

    public SalaryStructure toEntity(SalaryRequestDto salaryRequestDto, Employee employee) {
        return SalaryStructure.builder()
                .employee(employee)
                .basicSalary(salaryRequestDto.getBasicSalary())
                .hra(salaryRequestDto.getHra())
                .specialAllowance(salaryRequestDto.getSpecialAllowance())
                .pfEmployee(salaryRequestDto.getPfEmployee())
                .pfEmployer(salaryRequestDto.getPfEmployer())
                .professionalTax(salaryRequestDto.getProfessionalTax())
                .tds(salaryRequestDto.getTds())
                .effectiveFrom(salaryRequestDto.getEffectiveFrom())
                .effectiveTo(salaryRequestDto.getEffectiveTo())
                .build();
    }
}