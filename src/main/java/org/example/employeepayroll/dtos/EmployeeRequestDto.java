package org.example.employeepayroll.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.example.employeepayroll.entities.EmployeeType;
import org.example.employeepayroll.validators.ContactNumber;

import java.time.LocalDate;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequestDto {
    @NotBlank
    private String employeeCode;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @Email
    private String email;
    @ContactNumber
    private String phoneNumber;
    @NotBlank
    private String designation;
    @NotNull
    private LocalDate dateOfJoining;
    @NotNull
    private EmployeeType employeeType;
    @NotNull
    private Long departmentId;
    private Long managerId;
}
