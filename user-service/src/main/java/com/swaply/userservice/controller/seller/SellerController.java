package com.swaply.userservice.controller.seller;

import com.swaply.userservice.dto.ApiResponse;
import com.swaply.userservice.dto.seller.SellerDashboardDto;
import com.swaply.userservice.entity.Seller;
import com.swaply.userservice.service.seller.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    /**
     * Get seller dashboard with stats and orders
     */
    @GetMapping("/dashboard/{sellerId}")
    public ResponseEntity<ApiResponse<SellerDashboardDto>> getSellerDashboard(
            @PathVariable UUID sellerId) {
        SellerDashboardDto dashboard = sellerService.getSellerDashboard(sellerId);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @PutMapping("/order/{orderId}/status")
    public ResponseEntity<ApiResponse<Void>> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestParam String status) {
        sellerService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * Update seller's profile photo
     */
    @PutMapping("/profile/photo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changeProfilePhoto(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        sellerService.changeProfilePhoto(file, authentication);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * Update seller's banner image
     */
    @PutMapping("/profile/banner")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changeBanner(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        sellerService.changeBanner(file, authentication);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * Get seller profile information
     */
    @GetMapping("/profile/{sellerId}")
    public ResponseEntity<ApiResponse<Seller>> getSellerProfile(
            @PathVariable UUID sellerId) {
        Seller seller = sellerService.getSellerProfile(sellerId);
        return ResponseEntity.ok(ApiResponse.success(seller));
    }

    /**
     * Update seller profile information
     */
    @PutMapping("/profile/{sellerId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Seller>> updateSellerProfile(
            @PathVariable UUID sellerId,
            @RequestBody Seller updatedSeller,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        Seller seller = sellerService.updateSellerProfile(sellerId, updatedSeller);
        return ResponseEntity.ok(ApiResponse.success(seller));
    }

    /**
     * Search sellers by username pattern
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<java.util.List<com.swaply.userservice.dto.seller.SellerSearchDto>>> searchSellers(
            @RequestParam(required = false, defaultValue = "") String query) {
        java.util.List<com.swaply.userservice.dto.seller.SellerSearchDto> results = sellerService.searchSellersByUsernamePattern(query);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}

