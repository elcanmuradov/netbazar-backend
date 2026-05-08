package com.swaply.userservice.config;

import com.swaply.userservice.entity.Admin;
import com.swaply.userservice.repository.AdminRepository;
import com.swaply.userservice.utils.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminDataInitializer {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String DEFAULT_ADMIN_EMAIL = "admin@netbazar.me";

    @Bean
    public CommandLineRunner initAdmin() {
        return args -> {
            if (!adminRepository.existsByEmail(DEFAULT_ADMIN_EMAIL)) {
                Admin defaultAdmin = Admin.builder()
                        .name("Admin")
                        .email(DEFAULT_ADMIN_EMAIL)
                        .password(passwordEncoder.encode("admin123"))
                        .role(UserRole.ADMIN)
                        .build();

                adminRepository.save(defaultAdmin);
                log.info("Default admin account created: {} / admin123", DEFAULT_ADMIN_EMAIL);
            } else {
                log.info("Admin accounts already exist. Skipping initialization.");
            }
        };
    }
}
