package org.example.employeepayroll.mappers;

import org.example.employeepayroll.dtos.AuthRequestDto;
import org.example.employeepayroll.dtos.AuthResponseDto;
import org.example.employeepayroll.entities.Role;
import org.example.employeepayroll.entities.Users;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public AuthMapper(BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public Users toEntity(AuthRequestDto dto) {
        return Users.builder()
                .username(dto.getUsername())
                .password(bCryptPasswordEncoder.encode(dto.getPassword()))
                .role(Role.GUEST)
                .build();
    }

    public AuthResponseDto toResponse(Users user) {
        return AuthResponseDto.builder()
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    public AuthResponseDto toResponse(UserDetails userDetails) {
        return AuthResponseDto.builder()
                .username(userDetails.getUsername())
                .role(Role.valueOf(userDetails.getAuthorities().iterator().next().getAuthority()))
                .build();
    }
}
