package org.eample.employeepayoll.dtos;

import lombok.*;
import org.eample.employeepayoll.entities.EmployeeType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponseDto {
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate dateOfJoining;
    private String designation;
    private EmployeeType employeeType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
