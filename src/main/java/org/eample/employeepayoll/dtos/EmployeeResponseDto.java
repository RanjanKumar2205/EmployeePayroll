package org.eample.employeepayoll.dtos;

import lombok.*;
import org.eample.employeepayoll.entities.EmployeeType;
import org.eample.employeepayoll.entities.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponseDto {
    private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate dateOfJoining;
    private String designation;
    private EmployeeType employeeType;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long departmentId;
    private String departmentName;
}
