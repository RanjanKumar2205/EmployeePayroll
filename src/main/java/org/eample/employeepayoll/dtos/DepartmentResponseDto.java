package org.eample.employeepayoll.dtos;

import lombok.*;
import org.eample.employeepayoll.entities.Status;

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
}
