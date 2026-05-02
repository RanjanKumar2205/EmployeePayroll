package org.example.employeepayroll.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalaryRequestDto {
    @NotNull
    private Long employeeId;
    @NotNull
    private Double basicSalary;
    @NotNull
    private Double hra;
    @NotNull
    private Double specialAllowance;
    @NotNull
    private Double pfEmployee;
    @NotNull
    private Double pfEmployer;
    @NotNull
    private Double professionalTax;
    @NotNull
    private Double tds;
    @NotNull
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}
