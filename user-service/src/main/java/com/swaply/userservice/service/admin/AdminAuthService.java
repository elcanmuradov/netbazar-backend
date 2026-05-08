package com.swaply.userservice.service.admin;

import com.swaply.userservice.dto.admin.AdminAuthResponse;
import com.swaply.userservice.dto.admin.LoginAdminRequest;
import com.swaply.userservice.dto.admin.RegisterAdminRequest;
import com.swaply.userservice.entity.Admin;
import com.swaply.userservice.exception.AuthException;
import com.swaply.userservice.repository.AdminRepository;
import com.swaply.userservice.service.JwtService;
import com.swaply.userservice.utils.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminAuthService {
    private final AdminRepository adminRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    public AdminAuthResponse login(@RequestBody LoginAdminRequest loginRequest) {
        log.info("Admin login request : {}", loginRequest);

        Admin admin = adminRepository.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new AuthException("Admin not found"));


        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );


        String token = jwtService.generateToken(admin);
        return AdminAuthResponse.builder().token(token).build();
    }

    public AdminAuthResponse register(RegisterAdminRequest registerRequest) {
        log.info("Register request : {}", registerRequest);
        Admin admin = Admin.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.ADMIN)
                .build();

        admin = adminRepository.save(admin);


        return AdminAuthResponse.builder().token(jwtService.generateToken(admin)).build();

    }

    public void logout(String authorizationHeader) {
        log.info("Logout request : {}", authorizationHeader);
        long expirationTime = jwtService.getExpirationTime(authorizationHeader);
        String token = jwtService.extractToken(authorizationHeader);
        String blacklistKey = "blacklist:token:" + token;

        redisTemplate.opsForValue().set(
                blacklistKey,
                "logout",
                expirationTime,
                TimeUnit.MILLISECONDS
        );
    }
}
