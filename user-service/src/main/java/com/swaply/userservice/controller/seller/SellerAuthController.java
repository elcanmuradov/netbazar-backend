package com.swaply.userservice.controller.seller;

import com.swaply.userservice.dto.ApiResponse;
import com.swaply.userservice.dto.user.create.AuthResponse;
import com.swaply.userservice.dto.user.create.LoginRequest;
import com.swaply.userservice.dto.user.create.RegisterRequest;
import com.swaply.userservice.service.seller.SellerAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seller/auth")
@RequiredArgsConstructor
public class SellerAuthController {
    private final SellerAuthService sellerAuthService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> sellerRegister(@RequestBody @Valid RegisterRequest registerRequest) {
        return ResponseEntity.ok(ApiResponse.success(sellerAuthService.registerSeller(registerRequest)));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String authorizationHeader) {
        sellerAuthService.logout(authorizationHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(ApiResponse.success(sellerAuthService.login(loginRequest)));
    }
}
