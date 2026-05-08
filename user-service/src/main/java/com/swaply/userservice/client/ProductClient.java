package com.swaply.userservice.client;

import com.swaply.userservice.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.UUID;


@FeignClient(name = "product-service", url = "http://product-service:8080")
@Component
public interface ProductClient {
    @GetMapping("/product-count")
    ApiResponse<Long> getProductCount();

    @GetMapping("/product/isActive")
    ApiResponse<Boolean> isActiveProduct(@RequestParam("uuid") UUID productId);

    @GetMapping("/seller/products/active")
    ApiResponse<Long> getSellerActiveProducts(@RequestParam("sellerId") UUID sellerId);

    @GetMapping("/seller/orders")
    ApiResponse<?> getSellerOrders(@RequestParam("sellerId") UUID sellerId);

    @GetMapping("/seller/stats")
    ApiResponse<?> getSellerStats(@RequestParam("sellerId") UUID sellerId);

    @GetMapping("/admin/reports/monthly")
    ApiResponse<Map<String, Object>> getMonthlyReport();

    @org.springframework.web.bind.annotation.PutMapping("/orders/{orderId}/status")
    ApiResponse<?> updateOrderStatus(@org.springframework.web.bind.annotation.PathVariable("orderId") UUID orderId, @RequestParam("status") String status);
}
