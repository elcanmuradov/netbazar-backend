package com.swaply.userservice.service.seller;

import com.swaply.userservice.dto.user.create.AuthResponse;
import com.swaply.userservice.dto.user.create.LoginRequest;
import com.swaply.userservice.dto.user.create.RegisterRequest;
import com.swaply.userservice.entity.Seller;
import com.swaply.userservice.entity.User;
import com.swaply.userservice.exception.AuthException;
import com.swaply.userservice.repository.SellerRepository;
import com.swaply.userservice.service.JwtService;
import com.swaply.userservice.utils.enums.AccountStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerAuthService {

    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AuthenticationManager authenticationManager;

    public AuthResponse registerSeller(RegisterRequest registerRequest) {
        log.info("Seller register request : {}", registerRequest);

        if (sellerRepository.findByPhone(registerRequest.getPhone()).isPresent()) {
            throw new AuthException("Telefon nömrəsi qeydiyyatda var");
        }

        if (sellerRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new AuthException("Email artıq qeydiyyatdan keçib");
        }

        Seller seller = Seller.builder()
                .name(registerRequest.getName())
                .username(registerRequest.getEmail())
                .email(registerRequest.getEmail())
                .phone(registerRequest.getPhone())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .productCount(0L)
                .paymentsTotal(BigDecimal.valueOf(0))
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        seller = sellerRepository.save(seller);

        return AuthResponse.builder().token(jwtService.generateToken(seller)).build();
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

    public AuthResponse login(LoginRequest loginRequest) {
        log.info("Login request : {}", loginRequest);

        Seller seller = sellerRepository.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new AuthException("İstifadəçi tapılmadı: " + loginRequest.getEmail()));


        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );



        String token = jwtService.generateToken(seller);
        return AuthResponse.builder().token(token).build();
    }
}
