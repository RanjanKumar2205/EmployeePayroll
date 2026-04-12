package org.example.employeepayroll.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRequestDto {
    @NotBlank
    private String name;
    @NotBlank
    private String code;
}
