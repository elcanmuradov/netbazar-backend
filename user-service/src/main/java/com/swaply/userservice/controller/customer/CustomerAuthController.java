package com.swaply.userservice.controller.customer;

import com.swaply.userservice.dto.ApiResponse;
import com.swaply.userservice.dto.user.create.AuthResponse;
import com.swaply.userservice.dto.user.create.LoginRequest;
import com.swaply.userservice.dto.user.create.RegisterRequest;
import com.swaply.userservice.dto.user.create.VerificationRequest;
import com.swaply.userservice.dto.user.update.ChangePasswordRequest;
import com.swaply.userservice.service.customer.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class CustomerAuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(loginRequest)));
    }

    @PostMapping("/send-code")
    public void sendVerificationCode(@RequestBody Map<String, String> request) {

        authService.sendVerificationCode(request);
    }

    @PostMapping("/verify")
    public boolean verify(@Valid @RequestBody VerificationRequest verificationRequest) {
        return authService.verify(verificationRequest);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody @Valid RegisterRequest registerRequest) {
        return ResponseEntity.ok(ApiResponse.success(authService.register(registerRequest)));
    }



    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public void logout(
            @RequestHeader("Authorization") String authorizationHeader) {
        authService.logout(authorizationHeader);
    }

    @PutMapping("/change-password")
    public void getUserProfile(Authentication authentication,@RequestBody ChangePasswordRequest request){
        if (authentication == null) {
            log.error("Authentication object is null");
        }
       authService.changePassword(authentication,request);
    }

}
