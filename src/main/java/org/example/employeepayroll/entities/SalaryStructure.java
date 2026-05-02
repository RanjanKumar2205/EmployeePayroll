package org.example.employeepayroll.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "salary_structure")
public class SalaryStructure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private Double basicSalary;

    private Double hra;

    private Double specialAllowance;

    private Double pfEmployee;

    private Double pfEmployer;

    private Double professionalTax;

    private Double tds;

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
    @Builder.Default
    private boolean isActive = true;
}
