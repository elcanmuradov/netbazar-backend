package com.swaply.orderservice.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.swaply.orderservice.dto.ApiResponse;

@Component
@FeignClient(name = "product-service", url = "http://product-service:8080")
public interface ProductClient {

    @GetMapping("/product/{productId}")
    ApiResponse<ProductSummary> getProductById(@PathVariable("productId") UUID productId);

    @GetMapping("/user/{userId}/product/status/{status}")
    ApiResponse<List<ProductSummary>> getUserProductsByStatus(
            @PathVariable("userId") UUID userId,
            @PathVariable("status") String status);

    record ProductSummary(UUID id, UUID userId, String title, Double price) {
        public BigDecimal priceAsBigDecimal() {
            return price == null ? BigDecimal.ZERO : BigDecimal.valueOf(price);
        }
    }
}
