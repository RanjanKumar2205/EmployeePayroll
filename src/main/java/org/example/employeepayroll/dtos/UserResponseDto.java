package org.example.employeepayroll.dtos;

import lombok.*;
import org.example.employeepayroll.entities.Role;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private String userName;
    private Role role;
}
