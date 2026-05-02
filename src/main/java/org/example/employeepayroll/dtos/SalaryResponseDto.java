package org.example.employeepayroll.dtos;

import lombok.*;

import java.time.LocalDate;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalaryResponseDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private Double basicSalary;
    private Double hra;
    private Double specialAllowance;
    private Double pfEmployee;
    private Double pfEmployer;
    private Double professionalTax;
    private Double tds;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private boolean isActive;
}
