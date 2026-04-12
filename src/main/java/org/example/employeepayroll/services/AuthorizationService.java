package org.example.employeepayroll.services;

import org.example.employeepayroll.repositories.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service("authorizationService")
public class AuthorizationService {
    private final UserRepository userRepository;

    public AuthorizationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isOwner(Long employeeId) {
        String username = Objects.requireNonNull(SecurityContextHolder.getContext()
                .getAuthentication()).getName();
        return userRepository.findByUsername(username)
                .map(user -> user.getEmployee() != null
                        && user.getEmployee().getId().equals(employeeId))
                .orElse(false);
    }
}
