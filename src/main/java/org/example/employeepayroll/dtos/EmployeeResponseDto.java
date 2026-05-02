package org.example.employeepayroll.dtos;

import lombok.*;
import org.example.employeepayroll.entities.EmployeeType;
import org.example.employeepayroll.entities.Status;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
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
    private String createdBy;
    private String lastModifiedBy;
}
