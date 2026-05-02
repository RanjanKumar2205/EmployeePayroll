package org.example.employeepayroll.config;

import org.example.employeepayroll.entities.Role;
import org.example.employeepayroll.entities.Users;
import org.example.employeepayroll.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements ApplicationRunner {
   private final String adminUsername;
   private final String adminPassword;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder,
                      @Value("${app.admin.username}") String adminUsername,
                      @Value("${app.admin.password}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.findByRole(Role.ADMIN).isEmpty()) {
            Users admin = Users.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .isProtected(true)
                    .build();
            userRepository.save(admin);
        }
    }
}