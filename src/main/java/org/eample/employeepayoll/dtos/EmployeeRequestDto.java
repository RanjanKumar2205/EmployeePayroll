package org.eample.employeepayoll.dtos;

import lombok.*;
import org.eample.employeepayoll.entities.EmployeeType;

import java.time.LocalDate;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequestDto {
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String designation;
    private LocalDate dateOfJoining;
    private EmployeeType employeeType;
}
