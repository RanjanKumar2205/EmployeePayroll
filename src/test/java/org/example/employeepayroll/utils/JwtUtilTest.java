package org.example.employeepayroll.utils;

import org.example.employeepayroll.entities.Role;
import org.example.employeepayroll.entities.Users;
import org.example.employeepayroll.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtUtilTest {
    private JwtUtil jwtUtil;
    private UserDetails userDetails;
    private static final String TEST_SECRET = "test-secret-key-must-be-at-least-32-characters-long";
    private static final Long TEST_EXPIRY = 3600L;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(TEST_SECRET, TEST_EXPIRY);

        Users user = Users.builder()
                .id(1L)
                .username("ranjan@example.com")
                .password("encodedPassword")
                .role(Role.EMPLOYEE)
                .build();

        userDetails = new UserPrincipal(user);
    }

    @Test
    void generateToken_shouldReturnNonEmptyToken() {
        String token = jwtUtil.generateToken(userDetails);

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
        // JWT always has 3 parts separated by dots
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        String token = jwtUtil.generateToken(userDetails);

        String extractedUsername = jwtUtil.extractUsername(token);

        assertThat(extractedUsername).isEqualTo("ranjan@example.com");
    }

    @Test
    void validateToken_withCorrectUser_shouldReturnTrue() {
        String token = jwtUtil.generateToken(userDetails);

        boolean isValid = jwtUtil.validateToken(token, userDetails);

        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_withWrongUser_shouldReturnFalse() {
        String token = jwtUtil.generateToken(userDetails);

        // Different user — same structure but different username
        Users differentUser = Users.builder()
                .id(2L)
                .username("someone.else@example.com")
                .password("encodedPassword")
                .role(Role.EMPLOYEE)
                .build();
        UserDetails differentUserDetails = new UserPrincipal(differentUser);

        boolean isValid = jwtUtil.validateToken(token, differentUserDetails);

        assertThat(isValid).isFalse();
    }

    @Test
    void extractExpiration_shouldReturnFutureDate() {
        String token = jwtUtil.generateToken(userDetails);

        Date expiration = jwtUtil.extractExpiration(token);

        assertThat(expiration).isNotNull();
        // Expiration should be in the future
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    void validateToken_withExpiredToken_shouldReturnFalse() {
        // Create JwtUtil with 0-second expiry — token expires immediately
        JwtUtil expiredJwtUtil = new JwtUtil(TEST_SECRET, 0L);
        String token = expiredJwtUtil.generateToken(userDetails);

        // Small sleep to ensure expiry has passed
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}

        boolean isValid = expiredJwtUtil.validateToken(token, userDetails);

        assertThat(isValid).isFalse();
    }
}
