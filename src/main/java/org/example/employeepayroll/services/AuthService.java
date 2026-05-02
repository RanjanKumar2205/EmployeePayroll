package org.example.employeepayroll.services;

import org.example.employeepayroll.dtos.AuthRequestDto;
import org.example.employeepayroll.dtos.AuthResponseDto;
import org.example.employeepayroll.entities.Employee;
import org.example.employeepayroll.entities.Role;
import org.example.employeepayroll.entities.Users;
import org.example.employeepayroll.repositories.EmployeeRepository;
import org.example.employeepayroll.exceptions.DuplicateResourceException;
import org.example.employeepayroll.mappers.AuthMapper;
import org.example.employeepayroll.repositories.UserRepository;
import org.example.employeepayroll.utils.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthMapper authMapper;
    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, AuthMapper authMapper,
                       AuthenticationManager authenticationManager, EmployeeRepository employeeRepository) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.authMapper = authMapper;
        this.authenticationManager = authenticationManager;
        this.employeeRepository = employeeRepository;
    }

    public AuthResponseDto register(AuthRequestDto dto) {
        if(userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Username already exists: " + dto.getUsername());
        }
        Users user = authMapper.toEntity(dto);

        Optional<Employee> employee = employeeRepository.findByEmail(dto.getUsername());
        if(employee.isPresent()) {
            user.setRole(Role.EMPLOYEE);
            user.setEmployee(employee.get());
        }
        userRepository.save(user);

        return authMapper.toResponse(user);
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

        AuthResponseDto response = authMapper.toResponse(userDetails);
        response.setToken(token);

        return response;
    }
}
