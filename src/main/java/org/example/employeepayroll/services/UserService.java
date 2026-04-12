package org.example.employeepayroll.services;

import org.example.employeepayroll.dtos.RoleUpdateDto;
import org.example.employeepayroll.dtos.UserResponseDto;
import org.example.employeepayroll.entities.User;
import org.example.employeepayroll.exceptions.ResourceNotFoundException;
import org.example.employeepayroll.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponseDto updateRole(Long id, RoleUpdateDto dto, Authentication auth) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String currentUsername = auth.getName();
        if (user.getIsProtected()) {
            throw new IllegalArgumentException("This account is protected and cannot be modified");
        }
        if (user.getUsername().equals(currentUsername)) {
            throw new IllegalArgumentException("You cannot change your own role");
        }

        user.setRole(dto.getRole());
        userRepository.save(user);
        return UserResponseDto.builder()
                .userName(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
