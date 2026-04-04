package org.eample.employeepayoll.dtos;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSummaryDto {
    private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String designation;
}
