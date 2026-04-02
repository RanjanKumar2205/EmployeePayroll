package org.eample.employeepayoll.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
    private String employeeCode;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @Email
    private String email;
    @ContactNumber
    private String phoneNumber;
    private String designation;
    private LocalDate dateOfJoining;
    private EmployeeType employeeType;
}
