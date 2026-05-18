package com.facetrack.service;

import com.facetrack.config.AppProperties;
import com.facetrack.entity.User;
import com.facetrack.enums.UserRole;
import com.facetrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    @Override
    public void run(String... args) {
        String email = appProperties.getSeed().getAdminEmail();
        if (userRepository.findByEmail(email).isPresent()) {
            log.info("Admin '{}' already exists - skipping seed.", email);
            return;
        }

        User admin = User.builder()
                .email(email)
                .name(appProperties.getSeed().getAdminName())
                .passwordHash(passwordEncoder.encode(appProperties.getSeed().getAdminPassword()))
                .role(UserRole.ADMIN)
                .build();
        userRepository.save(admin);
        log.info("Seeded admin: email={}", email);
    }
}
