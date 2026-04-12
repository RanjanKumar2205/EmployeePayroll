package org.example.employeepayroll.dtos;

import lombok.Getter;
import lombok.Setter;
import org.example.employeepayroll.entities.Role;

@Getter
@Setter
public class RoleUpdateDto {
    private Role role;
}
