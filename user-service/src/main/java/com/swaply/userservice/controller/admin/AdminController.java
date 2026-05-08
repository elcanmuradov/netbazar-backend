package com.swaply.userservice.controller.admin;

import com.swaply.userservice.dto.ApiResponse;
import com.swaply.userservice.dto.admin.DashboardStatsDto;
import com.swaply.userservice.dto.admin.message.ReportMessageDto;
import com.swaply.userservice.dto.user.UserDto;
import com.swaply.userservice.entity.Seller;
import com.swaply.userservice.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/product-count")
    public ResponseEntity<ApiResponse<Long>> getProductCount() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getProductCount()));
    }

    @GetMapping("/user-count")
    public ResponseEntity<ApiResponse<Long>> getUserCount() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUserCount()));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDashboardStats()));
    }

    @GetMapping("/reports/monthly")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlyReport() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getMonthlyReport()));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDto>>> getUsers() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUsers()));
    }

    @PutMapping("/users/{userId}/ban")
    public void banUser(@PathVariable UUID userId, @RequestParam Long seconds) {
        adminService.banUser(userId, seconds);
    }

    @PutMapping("/users/{userId}/unban")
    public void unBanUser(@PathVariable UUID userId) {
        adminService.unBanUser(userId);
    }

    @GetMapping("/sellers")
    public ResponseEntity<ApiResponse<List<Seller>>> getSellers() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllSellers()));
    }

    @GetMapping("/sellers/{sellerId}")
    public ResponseEntity<ApiResponse<Seller>> getSeller(@PathVariable UUID sellerId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getSellerById(sellerId)));
    }

    @PutMapping("/sellers/{sellerId}/activate")
    public ResponseEntity<ApiResponse<Void>> activateSeller(@PathVariable UUID sellerId) {
        adminService.activateSeller(sellerId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/sellers/{sellerId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateSeller(@PathVariable UUID sellerId) {
        adminService.deactivateSeller(sellerId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/sellers/{sellerId}")
    public ResponseEntity<ApiResponse<Seller>> updateSeller(
            @PathVariable UUID sellerId,
            @RequestBody Seller seller) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updateSeller(sellerId, seller)));
    }

    @PutMapping("/sellers/{sellerId}/penalize")
    public ResponseEntity<ApiResponse<Void>> penalizeSeller(
            @PathVariable UUID sellerId,
            @RequestParam String reason) {
        adminService.penalizeSeller(sellerId, reason);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/sellers/{sellerId}/commission")
    public ResponseEntity<ApiResponse<Void>> setSellerCommission(
            @PathVariable UUID sellerId,
            @RequestParam BigDecimal commissionRate) {
        adminService.setSellerCommission(sellerId, commissionRate);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/sellers/{sellerId}/payout")
    public ResponseEntity<ApiResponse<Map<String, Object>>> processPayout(
            @PathVariable UUID sellerId,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(ApiResponse.success(adminService.processPayout(sellerId, amount)));
    }

    @GetMapping("/payouts")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPayouts() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getPayoutHistory()));
    }

    @GetMapping("/reported-messages")
    public ResponseEntity<ApiResponse<List<ReportMessageDto>>> getReportedMessages() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getReportedMessages()));
    }

    @PutMapping("/reported-messages/{messageId}/ban")
    public void setBanned(@PathVariable String messageId) {
        adminService.setBanned(messageId);
    }

    @PutMapping("/reported-messages/{messageId}/resolve")
    public void setResolved(@PathVariable String messageId) {
        adminService.setResolved(messageId);
    }

    @DeleteMapping("/stats/products/delete-all")
    public void deleteAllProducts() {
        adminService.deleteAllResources();
    }
}
