package org.example.employeepayroll.mappers;

import org.example.employeepayroll.dtos.AuthResponseDto;
import org.example.employeepayroll.dtos.RegisterRequestDto;
import org.example.employeepayroll.entities.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public AuthMapper(BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public User toEntity(RegisterRequestDto dto) {
        return User.builder()
                .username(dto.getUsername())
                .password(bCryptPasswordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .build();
    }

    public AuthResponseDto toResponse(User user) {
        return AuthResponseDto.builder()
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
