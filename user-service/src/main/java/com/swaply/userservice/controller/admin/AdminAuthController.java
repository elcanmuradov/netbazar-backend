package com.swaply.userservice.controller.admin;

import com.swaply.userservice.dto.ApiResponse;
import com.swaply.userservice.dto.admin.AdminAuthResponse;
import com.swaply.userservice.dto.admin.LoginAdminRequest;
import com.swaply.userservice.dto.admin.RegisterAdminRequest;
import com.swaply.userservice.service.admin.AdminAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor()
public class AdminAuthController {
    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AdminAuthResponse>> login(@RequestBody LoginAdminRequest loginAdminRequest) {
        return ResponseEntity.ok(ApiResponse.success(adminAuthService.login(loginAdminRequest)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AdminAuthResponse>> register(@RequestBody RegisterAdminRequest adminRequest) {
        return ResponseEntity.ok(ApiResponse.success(adminAuthService.register(adminRequest)));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public void logout(@RequestHeader("Authorization") String authorizationHeader) {
        adminAuthService.logout(authorizationHeader);
    }
}
