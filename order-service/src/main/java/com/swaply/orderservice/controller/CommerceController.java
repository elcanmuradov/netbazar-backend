package com.swaply.orderservice.controller;

import com.swaply.orderservice.dto.ApiResponse;
import com.swaply.orderservice.dto.commerce.CreateBulkOrderRequest;
import com.swaply.orderservice.dto.commerce.CreateOrderRequest;
import com.swaply.orderservice.dto.commerce.CreateReviewRequest;
import com.swaply.orderservice.dto.commerce.OrderDto;
import com.swaply.orderservice.dto.commerce.ReviewDto;
import com.swaply.orderservice.service.CommerceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class CommerceController {

    private final CommerceService commerceService;

    public CommerceController(CommerceService commerceService) {
        this.commerceService = commerceService;
    }

    @GetMapping("/user/{userId}/orders")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getOrders(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(commerceService.getOrders(userId)));
    }

    @PostMapping("/user/{userId}/orders")
    public ResponseEntity<ApiResponse<OrderDto>> createOrder(@PathVariable UUID userId, @RequestBody @Valid CreateOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success(commerceService.createOrder(userId, request)));
    }

    @PostMapping("/user/{userId}/orders/bulk")
    public ResponseEntity<ApiResponse<List<OrderDto>>> createBulkOrder(@PathVariable UUID userId, @RequestBody @Valid CreateBulkOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success(commerceService.createBulkOrder(userId, request)));
    }

    @GetMapping("/product/{productId}/reviews")
    public ResponseEntity<ApiResponse<List<ReviewDto>>> getReviews(@PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.success(commerceService.getReviews(productId)));
    }

    @GetMapping("/seller/orders")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getSellerOrders(@RequestParam UUID sellerId) {
        return ResponseEntity.ok(ApiResponse.success(commerceService.getSellerOrders(sellerId)));
    }

    @GetMapping("/seller/stats")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getSellerStats(@RequestParam UUID sellerId) {
        return ResponseEntity.ok(ApiResponse.success(commerceService.getSellerStats(sellerId)));
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderDto>> updateOrderStatus(@PathVariable UUID orderId, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success(commerceService.updateOrderStatus(orderId, status)));
    }

    @PostMapping("/user/{userId}/product/{productId}/reviews")
    public ResponseEntity<ApiResponse<ReviewDto>> createReview(
            @PathVariable UUID userId,
            @PathVariable UUID productId,
            @RequestBody @Valid CreateReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success(commerceService.createReview(productId, userId, request.getUserName(), request)));
    }
}
