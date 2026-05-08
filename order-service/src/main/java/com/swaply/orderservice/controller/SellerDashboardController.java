package com.swaply.orderservice.controller;

import com.swaply.orderservice.dto.ApiResponse;
import com.swaply.orderservice.dto.commerce.OrderDto;
import com.swaply.orderservice.service.CommerceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerDashboardController {

    private final CommerceService commerceService;

    @GetMapping("/dashboard/{sellerId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSellerDashboard(@PathVariable UUID sellerId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("stats", commerceService.getSellerStats(sellerId));
        data.put("orders", commerceService.getSellerOrders(sellerId));
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PutMapping("/order/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderDto>> updateSellerOrderStatus(
            @PathVariable UUID orderId,
            @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success(commerceService.updateOrderStatus(orderId, status)));
    }

    @GetMapping("/orders/{sellerId}")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getOrders(@PathVariable UUID sellerId) {
        return ResponseEntity.ok(ApiResponse.success(commerceService.getSellerOrders(sellerId)));
    }
}
