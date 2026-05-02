package org.example.employeepayroll.controllers;

import org.example.employeepayroll.dtos.RoleUpdateDto;
import org.example.employeepayroll.dtos.UserResponseDto;
import org.example.employeepayroll.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("{id}/role")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody RoleUpdateDto dto, Authentication auth) {
        UserResponseDto responseDto = userService.updateRole(id, dto, auth);
        return ResponseEntity.ok(responseDto);
    }
}
