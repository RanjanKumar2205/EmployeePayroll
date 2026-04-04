package org.eample.employeepayoll.dtos;

import lombok.*;
import org.eample.employeepayoll.entities.Employee;
import org.eample.employeepayoll.entities.Status;

import java.util.Collection;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponseDto {
    private Long id;
    private String name;
    private String code;
    private Status status;
//    private Collection<EmployeeSummaryDto> employee;
}
