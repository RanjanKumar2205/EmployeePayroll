package org.example.employeepayroll.dtos;

import lombok.*;
import org.example.employeepayroll.entities.Status;

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
