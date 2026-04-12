package org.example.employeepayroll.dtos;

import lombok.*;
import org.example.employeepayroll.entities.Role;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {
    private String token;
    private String username;
    private Role role;
}
