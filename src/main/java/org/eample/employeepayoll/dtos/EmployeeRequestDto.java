package org.eample.employeepayoll.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.eample.employeepayoll.entities.EmployeeType;
import org.eample.employeepayoll.validators.ContactNumber;

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
    @NotBlank
    private Long departmentId;
}
