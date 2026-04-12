package org.example.employeepayroll.services;

import org.example.employeepayroll.dtos.AuthRequestDto;
import org.example.employeepayroll.dtos.AuthResponseDto;
import org.example.employeepayroll.dtos.RegisterRequestDto;
import org.example.employeepayroll.entities.Role;
import org.example.employeepayroll.entities.User;
import org.example.employeepayroll.entities.UserPrincipal;
import org.example.employeepayroll.exceptions.DuplicateResourceException;
import org.example.employeepayroll.mappers.AuthMapper;
import org.example.employeepayroll.repositories.UserRepository;
import org.example.employeepayroll.utils.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthMapper authMapper;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, AuthMapper authMapper, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.authMapper = authMapper;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponseDto register(RegisterRequestDto dto) {
        if(userRepository.findByUsername(dto.getUsername())
                .isPresent()) {throw new DuplicateResourceException("Username already exists: " + dto.getUsername());}
        User user = authMapper.toEntity(dto);
        userRepository.save(user);

        // Generate token immediately — user is logged in after registering
        UserDetails userDetails = new UserPrincipal(user);
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponseDto.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    public AuthResponseDto login(AuthRequestDto dto) {
        // AuthenticationManager handles everything:
        // 1. Calls UserDetailsServiceImpl.loadUserByUsername()
        // 2. Compares BCrypt hash of incoming password with stored hash
        // 3. Throws BadCredentialsException if invalid
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getUsername(),
                        dto.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponseDto.builder()
                .token(token)
                .username(userDetails.getUsername())
                .role(Role.valueOf(userDetails.getAuthorities()
                        .iterator().next().getAuthority()))
                .build();
    }
}
